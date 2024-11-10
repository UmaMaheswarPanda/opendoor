package com.sumit.opendoor.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import com.sumit.opendoor.interceptors.MyCustomInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.azure.storage.blob.BlobContainerClientBuilder;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;


@Slf4j @Configuration
@EnableJpaRepositories(basePackages= {"com.sumit.opendoor.repository"}, entityManagerFactoryRef="priEntityManagerFactory")
@PropertySource("file:/home/soma/opendoor/application.properties")
@ComponentScan({"com"})
public class BeanConfiguration implements WebMvcConfigurer {

	@Autowired
	Environment env ;

	private AmazonS3 s3Client;
	@Autowired
	@Lazy
	private MyCustomInterceptor myCustomInterceptor;

	@Value("${hikari.isolation.level:TRANSACTION_REPEATABLE_READ}")
	private String isolationLevel;

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("/index");
	}
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(
						"/resources/**")
				.addResourceLocations("/resources/");
	}
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		List<String> apis = new ArrayList<>();
		apis.addAll(java.util.Arrays.asList(getTOBeExcludeSessionValidationAPis()));
		registry.addInterceptor(myCustomInterceptor).addPathPatterns("/**").excludePathPatterns(apis);
	}
	public String[] getTOBeExcludeSessionValidationAPis(){
		return env.getProperty("toBeSessionExcludedApis") != null ?  env.getProperty("toBeSessionExcludedApis").split(",") : new String[0];
	}
	@Bean(name="dataSource")
	@Primary
	public DataSource getHikariConnectionPool() throws Exception {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(env.getProperty("db.url"));
		config.setUsername(env.getProperty("db.username"));
		config.setPassword(env.getProperty("db.password").trim());
		config.setDriverClassName(env.getProperty("db.driver"));

		config.setMinimumIdle(Integer.parseInt(env.getProperty("hikari.minimumIdel").trim()));
		config.setMaximumPoolSize(Integer.parseInt(env.getProperty("hikari.maxPoolSize").trim()));
		config.setConnectionTimeout(Integer.parseInt(env.getProperty("hikari.connectionTimeout").trim()));
		config.setIdleTimeout(Integer.parseInt(env.getProperty("hikari.idleTimeOut").trim()));
		config.setMaxLifetime(Integer.parseInt(env.getProperty("hikari.maxLifetime").trim()));
		config.setKeepaliveTime(Integer.parseInt(env.getProperty("hikari.keepaliveTime").trim()));
		config.setTransactionIsolation(isolationLevel);

		config.setConnectionTestQuery("SELECT 1");
		config.setPoolName("openDoorConnectionPool");

		return new HikariDataSource(config);

	}


	@Bean(name="priEntityManagerFactory")
	@Primary
	@Lazy
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();

		entityManagerFactory.setDataSource(dataSource);

		entityManagerFactory.setPackagesToScan("com.sumit.opendoor.entity");

		String db1PackagesToScan= env.getProperty("db1.entities.packagesToScan");

		if(!ObjectUtils.isEmpty(db1PackagesToScan))
		{
			entityManagerFactory.setPackagesToScan(db1PackagesToScan.split(","));
		}

		// Vendor adapter
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		entityManagerFactory.setJpaVendorAdapter(vendorAdapter);

		// Hibernate properties
		Properties additionalProperties = new Properties();
		additionalProperties.put("hibernate.dialect",
				env.getProperty("hibernate.dialect"));
		additionalProperties.put("hibernate.show_sql",
				env.getProperty("hibernate.show_sql"));
		additionalProperties.put("hibernate.hbm2ddl.auto",
				env.getProperty("hibernate.hbm2ddl.auto"));
		additionalProperties.put("connection.autoReconnectForPools", true);
		additionalProperties.put("connection.autoReconnect", true);
		additionalProperties.put("hibernate.enable_lazy_load_no_trans", true);
		entityManagerFactory.setPersistenceUnitName("primary");
		entityManagerFactory.setJpaProperties(additionalProperties);
		return entityManagerFactory;
	}


	@Bean(name="transactionManager")
	@Primary
	@Lazy
	public PlatformTransactionManager transactionManager(@Qualifier("priEntityManagerFactory")LocalContainerEntityManagerFactoryBean priEntityManagerFactory) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(priEntityManagerFactory
				.getObject());
		return transactionManager;
	}

	/**
	 * PersistenceExceptionTranslationPostProcessor is a bean post processor
	 * which adds an advisor to any bean annotated with Repository so that any
	 * platform-specific exceptions are caught and then rethrown as one Spring's
	 * unchecked data access exceptions (i.e. a subclass of
	 * DataAccessException).
	 */
	@Bean
	public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
		return new PersistenceExceptionTranslationPostProcessor();
	}

	@Autowired
	@Qualifier("dataSource")
	@Lazy
	private DataSource dataSource;

	@Bean
	public Validator validator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		return validator;
	}

	@Bean
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<Object, Object> template = new RedisTemplate<Object, Object>();
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setConnectionFactory(connectionFactory);
		return template;
	}
	@SuppressWarnings("deprecation")
	@Bean
	@Primary
	@Lazy
	public AmazonS3 initializeAmazon() {
		String region = env.getProperty("aws.region.name") == null ? "ap-south-1" : env.getProperty("aws.region.name");

		boolean isAwsDefaultAccessActive = Boolean.parseBoolean(env.getProperty("is.aws.default.access.active"));
		if (!isAwsDefaultAccessActive) {
			AWSCredentials credentials = new BasicAWSCredentials(env.getRequiredProperty("aws.access.key.id"),
					env.getRequiredProperty("aws.secret.access.key"));
			return this.s3Client = AmazonS3ClientBuilder.standard().withRegion(region)
					.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
		} else {
			return this.s3Client = AmazonS3ClientBuilder.standard().withRegion(region).build();
		}
	}

	@Bean
	@Lazy
	public AmazonS3 initializeAmazonTwo() {
		String region = env.getProperty("aws.region.name") == null ? "ap-south-1" : env.getProperty("aws.region.name");
		boolean isAwsDefaultAccessActive = Boolean.parseBoolean(env.getProperty("is.aws.default.access.active"));
		if (!isAwsDefaultAccessActive) {
			AWSCredentials credentials = new BasicAWSCredentials(env.getRequiredProperty("sftp.aws.access.key.id"),
					env.getRequiredProperty("sftp.aws.secret.access.key"));
			return this.s3Client = AmazonS3ClientBuilder.standard().withRegion(region)
					.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
		} else {
			return this.s3Client = AmazonS3ClientBuilder.standard().withRegion(region).build();
		}
	}

	@Bean
	public BlobContainerClientBuilder blobContainerClientBuilder() {
		try {
			return new BlobContainerClientBuilder()
					.connectionString(env.getProperty("blob.connection.string"))
					.containerName(env.getProperty("blob.container.string"));
		} catch (Exception e) {
			log.info("Error while creating BlobContainerClientBuilder", e);
			return new BlobContainerClientBuilder();
		}
	}
}
