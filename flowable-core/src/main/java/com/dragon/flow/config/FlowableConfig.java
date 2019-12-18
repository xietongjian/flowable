package com.dragon.flow.config;

import com.dragon.flow.flowable.listener.global.GlobalTaskCreateListener;
import com.dragon.flow.flowable.listener.global.GlobalTypeEventListener;
import com.dragon.tools.common.SpringContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.compatibility.spring.SpringFlowable5CompatibilityHandlerFactory;
import org.flowable.bpmn.converter.BpmnXMLConverter;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.impl.de.odysseus.el.misc.TypeConverter;
import org.flowable.common.engine.impl.de.odysseus.el.misc.TypeConverterImpl;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.spring.job.service.SpringAsyncExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @Description: flowable配置
 * @Author: Bruce.liu
 * @Since:18:44 2018/9/7
 */
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Value("${flowable.activityFontName}")
    private String activityFontName;
    @Value("${flowable.labelFontName}")
    private String labelFontName;
    @Value("${flowable.annotationFontName}")
    private String annotationFontName;
    @Value("${flowable.xml.encoding}")
    private String xmlEncoding;
    @Autowired
    private GlobalTypeEventListener globalTypeEventListener;
    @Autowired
    private GlobalTaskCreateListener globalTaskCreateListener;

    @Override
    public void configure(SpringProcessEngineConfiguration configure) {
        //配置中文
        configure.setActivityFontName(activityFontName);
        configure.setLabelFontName(labelFontName);
        configure.setAnnotationFontName(annotationFontName);
        //设置是否升级 false不升级  true升级
        configure.setDatabaseSchemaUpdate("true");
        //设置自定义的uuid生成策略
        configure.setIdGenerator(uuidGenerator());
        configure.setXmlEncoding(xmlEncoding);
        //全局监听
        Map<String, List<FlowableEventListener>> typedListeners = this.createGlobEventListeners();
        configure.setTypedEventListeners(typedListeners);
        //启用任务关系计数
        configure.setEnableTaskRelationshipCounts(true);
        //启动同步功能 一定要启动否则报错
        configure.setAsyncExecutor(springAsyncExecutor());
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public TypeConverter typeConverter() {
        return new TypeConverterImpl();
    }

    @Bean
    public UuidGenerator uuidGenerator() {
        return new UuidGenerator();
    }

    @Bean
    public SpringAsyncExecutor springAsyncExecutor() {
        SpringAsyncExecutor springAsyncExecutor = new SpringAsyncExecutor();
        springAsyncExecutor.setTaskExecutor(processTaskExecutor());
        springAsyncExecutor.setDefaultAsyncJobAcquireWaitTimeInMillis(1000);
        springAsyncExecutor.setDefaultTimerJobAcquireWaitTimeInMillis(1000);
        return springAsyncExecutor;
    }

    @Bean
    public TaskExecutor processTaskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    /**
     * BpmnXMLConverter
     *
     * @return BpmnXMLConverter
     */
    @Bean
    public BpmnXMLConverter createBpmnXMLConverter() {
        return new BpmnXMLConverter();
    }

    /**
     * BpmnJsonConverter
     *
     * @return BpmnJsonConverter
     */
    @Bean
    public BpmnJsonConverter createBpmnJsonConverter() {
        return new BpmnJsonConverter();
    }


    /**
     * 兼容V5
     *
     * @return
     */
    @Bean
    public SpringFlowable5CompatibilityHandlerFactory createSpringFlowable5CompatibilityHandlerFactory() {
        return new SpringFlowable5CompatibilityHandlerFactory();
    }

    /**
     * 创建全局的监听类
     *
     * @return
     */
    private Map<String, List<FlowableEventListener>> createGlobEventListeners() {
        Map<String, List<FlowableEventListener>> flowableEventListeners = new HashMap<>();
        //1、配置全局任务创建监听
        List<FlowableEventListener> createTasks = new ArrayList<>();
        globalTypeEventListener.setEventHandler(globalTaskCreateListener);
        createTasks.add(globalTypeEventListener);
        flowableEventListeners.put(FlowableEngineEventType.TASK_CREATED.name(), createTasks);

        //2、其他的
        return flowableEventListeners;
    }

    /**
     * 在配置文件中如果没有字段，使用@Value的时候就会忽略掉，不会报错
     *
     * @return
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }

    @Bean
    public SpringContextHolder creatSpringContextHolder() {
        SpringContextHolder springContextHolder = new SpringContextHolder();
        return springContextHolder;
    }
}
