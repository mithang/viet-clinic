package com.geeke.config.action;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.geeke.common.IActionSaver;

/**
 * Ngàynhật kýghicấu hìnhLớp
 * @author lys
 * version: 2021/11/16
 */

@Configuration
public class ActionSaverConfig {
	
	@Value("${spring.rabbitmq.enable: false}")
	private boolean enableMq;		// Có bật khôngmq

	/**
	 * spring.rabbitmq.enable=true giờ, Bậtthông báo队Cộtnhập kho; =false giờ, trực tiếpnhập kho
	 * @return
	 */
	@Bean(name = "actionSaver")
	public IActionSaver actionSaverBean() {
//		if(enableMq) {
//			return new MqActionSaver();
//		}

		return new DbActionSaver();
	}


	public boolean isEnableMq() {
		return enableMq;
	}


	public void setEnableMq(boolean enableMq) {
		this.enableMq = enableMq;
	}
	
}
