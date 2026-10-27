package com.geeke.uid;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baidu.fsg.uid.UidGenerator;
import com.baidu.fsg.uid.impl.CachedUidGenerator;
import com.baidu.fsg.uid.impl.DefaultUidGenerator;
import com.baidu.fsg.uid.worker.WorkerIdAssigner;
import com.geeke.utils.IdGen;

/**
 * @author: lys
 * @description: uidgenerator Cấu hình
 * @date: 2020/07/10 10:10
 */
@Configuration
public class UidConfiguration {
    // private static final Logger logger = LoggerFactory.getLogger(UidConfiguration.class);

   
    @Bean
    public UidGenerator uidGenerator(WorkerIdAssigner workerIdAssigner) {
    	DefaultUidGenerator generator = new CachedUidGenerator();
    	generator.setTimeBits(29);  // 28vịTối đacó thểhỗ trợ约8.7Năm
    	generator.setWorkerBits(21); // 22vịTối đacó thểhỗ trợ420万 lầnkhởi động lại
    	generator.setSeqBits(13);   // 13bit có thể hỗ trợ mỗi giây8192Cáivàphát
    	generator.setEpochStr("2020-07-23");
    	   	
    	generator.setWorkerIdAssigner(workerIdAssigner);
    	
    	// Cài đặtidsảnsinhthiết bị
        IdGen.setUidGenerator(generator);
    	return generator;
    }
}
