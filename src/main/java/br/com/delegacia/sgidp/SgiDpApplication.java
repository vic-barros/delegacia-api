package br.com.delegacia.sgidp;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
public class SgiDpApplication {

	public static void main(String[] args) {
		SpringApplication.run(SgiDpApplication.class, args);
	}

    @Bean(name = "cacheManager")
    public CacheManager cacheManager(){
        return new ConcurrentMapCacheManager("cacheDelegaciaApi");
    }

    @PostConstruct
    private void configTimeZone(){
        Locale.setDefault(Locale.forLanguageTag("pt_BR"));
        TimeZone sp = TimeZone.getTimeZone("America/Sao_Paulo");
        TimeZone.setDefault(sp);
        Calendar.getInstance().setTimeZone(sp);

        // System.out.println("Senha padrão: --------> " + passwordEncoder.encode("Senha
        // codificada no console"));
    }

}
