package com.dreamer.service.mobile.impl;

import com.dreamer.domain.user.AccountsRecord;
import com.dreamer.domain.user.AccountsTransfer;
import com.dreamer.domain.user.Agent;
import com.dreamer.domain.user.User;
import com.dreamer.domain.user.enums.AccountsTransferStatus;
import com.dreamer.domain.user.enums.AccountsType;
import com.dreamer.repository.mobile.AccountsRecordDao;
import com.dreamer.repository.mobile.AccountsTransferDao;
import com.dreamer.repository.mobile.AgentDao;
import com.dreamer.service.mobile.*;
import com.dreamer.util.CommonUtil;
import com.wxjssdk.util.XmlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ps.mx.otter.exception.ApplicationException;
import ps.mx.otter.utils.SearchParameter;

import java.util.*;

/**
 * Created by huangfei on 02/07/2017.
 */
@Service
public class AccountsTransferHandlerImpl extends BaseHandlerImpl<AccountsTransfer> implements AccountsTransferHandler {


    /**
     * @param fid
     * @param tid
     * @param typeState
     * @param amount
     */
    @Transactional
    @Override
    public void transferAccounts(Integer fid, Integer tid, Integer typeState, Double amount, String remark) {
        //组装订单
        Agent fromAgent = agentDao.get(fid);
        Agent toAgent = agentDao.get(tid);
        AccountsType accountsType = AccountsType.stateOf(typeState);
        AccountsTransfer accountsTransfer = new AccountsTransfer(toAgent, fromAgent, remark, amount, accountsType, new Date());
        accountsTransfer.setOut_trade_no(CommonUtil.createNo());//创建订单号 提交
        //转账业务 生成记录
        List<AccountsRecord> records = transfer(accountsTransfer);
        //保存转账订单
        accountsTransferDao.merge(accountsTransfer);
        //保存记录
        accountsRecordDao.saveList(records);
        //金额变动通知--通知失败 不会回滚 因为我捕获了异常 异步处理
        noticeHandler.noticeAccountRecords(records);
    }


    /**
     * 转券 生成记录
     *
     * @param accountsTransfer
     */
    private List<AccountsRecord> transfer(AccountsTransfer accountsTransfer) {
        accountsTransfer.setTransferTime(new Date());//设置和转出时间
        Agent from = accountsTransfer.getFromAgent();//转出方
        Agent to = accountsTransfer.getToAgent();//接收方
        if (from.getId().equals(to.getId())) {
            throw new ApplicationException("不能转给自己！");
        }
        List<AccountsRecord> records = new ArrayList<>();
        String info = accountsTransfer.getType().getStateInfo() + "转账-转给" + accountsTransfer.getToAgent().getRealName();
        //减少转出方的库存
        if (!from.isMutedUser()) {
//            accountsHandler.deductAccountAndRecord(AccountsType.VOUCHER, from, to, accountsTransfer.getAmount(), info);
            records.add(accountsHandler.deductAccountAndRecord(accountsTransfer.getType(), from, to, accountsTransfer.getAmount(), info));
        }
        //增加接收方的库存
        if (!to.isMutedUser()) {
            info = accountsTransfer.getType().getStateInfo() + "转账-来自" + accountsTransfer.getFromAgent().getRealName();
            records.add(accountsHandler.increaseAccountAndRecord(accountsTransfer.getType(), to, from, accountsTransfer.getAmount(), info));
        }
        //        increaseAccount(accountsTransfer.getType(), to, accountsTransfer.getAmount());
        //设置状态为主动转出
        accountsTransfer.setStatus(AccountsTransferStatus.TRANSFER);
        return records;
    }

    @Override
    public List<AccountsTransfer> findAccountsTransfer(SearchParameter<AccountsTransfer> parameter, User user) {
        return accountsTransferDao.findAccountsTransfer(parameter, user);
    }

    //充值业务 TODO 重复通知
    @Override
    @Transactional
    public String recharge(String body) {
        Map<String, String> xml = XmlUtil.xmlToMap(body);
        String out_trade_no = xml.get("out_trade_no");
        AccountsTransfer transfer =  get("out_trade_no",out_trade_no);
        if(transfer.getStatus()==AccountsTransferStatus.NEW){
            //转账业务 生成记录
            List<AccountsRecord> records = transfer(transfer);
            //保存转账订单
            accountsTransferDao.merge(transfer);
            //保存记录
            accountsRecordDao.saveList(records);
            //金额变动通知--通知失败 不会回滚 因为我捕获了异常 异步处理
            noticeHandler.noticeAccountRecords(records);
        }
        //返回支付结果
        Map<String, Object> map = new HashMap();
        map.put("return_code", "SUCCESS");
        map.put("return_msg", "OK");
        String result = XmlUtil.mapToXml(map);
        return result;
    }

    @Autowired
    private AgentDao agentDao;

    private AccountsTransferDao accountsTransferDao;

    @Autowired
    private AccountsRecordDao accountsRecordDao;

    @Autowired
    private AccountsHandler accountsHandler;

    @Autowired
    private NoticeHandler noticeHandler;

    @Autowired
    private AgentHandler agentHandler;

    @Autowired
    private MuteUserHandler muteUserHandler;


    public AccountsTransferDao getAccountsTransferDao() {
        return accountsTransferDao;
    }

    @Autowired
    public void setAccountsTransferDao(AccountsTransferDao accountsTransferDao) {
        this.accountsTransferDao = accountsTransferDao;
        super.setBaseDao(accountsTransferDao);
    }
}
