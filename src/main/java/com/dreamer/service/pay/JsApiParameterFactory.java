package com.dreamer.service.pay;

import com.dreamer.domain.wechat.WxConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class JsApiParameterFactory {


	/**
	 * 微信支付参数
	 * @param wxConfig
	 * @param prepayId
     * @return
     */
	public HashMap<String,Object> build(WxConfig wxConfig, String prepayId){
		HashMap<String,Object> jsapiParam=new HashMap<>();
		jsapiParam.put("appId", wxConfig.getAppid());
		jsapiParam.put("timeStamp", String.valueOf(System.currentTimeMillis()/1000));
		jsapiParam.put("nonceStr", RandomStringGenerator.getRandomStringByLength(32));
		jsapiParam.put("package", "prepay_id="+prepayId);
		jsapiParam.put("signType", "Encrypt");
		jsapiParam.put("paySign", Signature.getSign(jsapiParam, wxConfig.getKey()));
		return jsapiParam;
	}

	/**
	 * 微信地址参数
	 * @param wxConfig
	 * @param url
	 * @param accessToken
     * @return
     */
	public HashMap<String,Object> buildEditAddress(WxConfig wxConfig, String url, String accessToken){
		HashMap<String,Object> signParam=new HashMap<String,Object>();
		signParam.put("appid", wxConfig.getAppid());
		signParam.put("url", url);
		signParam.put("timestamp", String.valueOf(System.currentTimeMillis()/1000));
		signParam.put("noncestr", RandomStringGenerator.getRandomStringByLength(32));
		signParam.put("accesstoken", accessToken);
		String addrSign= Signature.getSHA1Sign(signParam);
		HashMap<String,Object> jsapiParam=new HashMap<String,Object>();
		jsapiParam.put("appId", signParam.get("appid"));
		jsapiParam.put("scope", "jsapi_address");
		jsapiParam.put("signType", "sha1");
		jsapiParam.put("addrSign",addrSign);
		jsapiParam.put("timeStamp", signParam.get("timestamp"));
		jsapiParam.put("nonceStr", signParam.get("noncestr"));
		return jsapiParam;
	}


	/**
	 *
	 * @param wxConfig  微信参数
	 * @param url  当前网页的地址 不包含#及其后面部分
	 * @param jsapi_ticket  零时票据
     * @return
     */
	public HashMap<String,Object> buildWxconfig(WxConfig wxConfig, String url, String jsapi_ticket){
		HashMap<String,Object> signParam=new HashMap<>();
		signParam.put("url", url);
		signParam.put("timestamp", String.valueOf(System.currentTimeMillis()/1000));
		signParam.put("noncestr", RandomStringGenerator.getRandomStringByLength(32));//小写
		signParam.put("jsapi_ticket", jsapi_ticket);
		String addrSign= Signature.getSHA1Sign(signParam);
		HashMap<String,Object> jsapiParam=new HashMap<String,Object>();
		jsapiParam.put("debug",false);
		jsapiParam.put("appId", wxConfig.getAppid());
		jsapiParam.put("timestamp", signParam.get("timestamp"));
		jsapiParam.put("nonceStr", signParam.get("noncestr"));//大写
		jsapiParam.put("signature",addrSign);
		return jsapiParam;
	}



	/**
	 * 分享连接参数
	 * @param url 当前网页的url
	 * @param 零时票据
     * @return
     */
	public HashMap<String,Object> buildShare(WxConfig wxConfig, String url, String jsapi_ticket){
		HashMap<String,Object> signParam=new HashMap<String,Object>();
		signParam.put("url", url);
		signParam.put("timestamp", String.valueOf(System.currentTimeMillis()/1000));
		signParam.put("noncestr", RandomStringGenerator.getRandomStringByLength(32));//小写
		signParam.put("jsapi_ticket", jsapi_ticket);
		String addrSign= Signature.getSHA1Sign(signParam);
		HashMap<String,Object> jsapiParam=new HashMap<String,Object>();
        jsapiParam.put("debug",false);
        jsapiParam.put("appId", wxConfig.getAppid());
        jsapiParam.put("timestamp", signParam.get("timestamp"));
        jsapiParam.put("nonceStr", signParam.get("noncestr"));//大写
		jsapiParam.put("signature",addrSign);
        ArrayList<String> jsApiList =new ArrayList<>();
        jsApiList.add("onMenuShareTimeline");
        jsApiList.add("onMenuShareAppMessage");
        jsapiParam.put("jsApiList",jsApiList);
		return jsapiParam;
	}


}
