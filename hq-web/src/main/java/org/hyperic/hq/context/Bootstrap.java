package org.hyperic.hq.context;

import java.util.Collection;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Bootstrap {

    private static final String[] APP_CONTEXT_FILES = new String[] { "classpath*:/META-INF/spring/app-*context.xml", 
                                                                     "classpath*:/META-INF/spring/jms-context.xml"};

    private static ApplicationContext APP_CONTEXT;
     

    public synchronized static ApplicationContext getContext() throws Exception {
        boolean initialize = false;
        if (APP_CONTEXT == null) {
            initialize = true;
            APP_CONTEXT = new ClassPathXmlApplicationContext(APP_CONTEXT_FILES,false);
        }
        if(initialize) {
            ((ConfigurableApplicationContext)APP_CONTEXT).refresh();
        }
        return APP_CONTEXT;
    }
    
   

    public static <T> T getBean(Class<T> beanClass)  {
        try {
            Collection<T> beans = getContext().getBeansOfType(beanClass).values();
            if(beans.isEmpty() && getContext().getParent() != null) {
                beans = getContext().getParent().getBeansOfType(beanClass).values();
            }
            T bean = beans.iterator().next();
            if (bean == null) {
                throw new IllegalArgumentException("Couldn't locate bean of " + beanClass + " type");
            }
            return bean;
        } catch (BeansException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public static Object getBean(String name) {
        try {
            Object bean = getContext().getBean(name);
            if(bean == null && getContext().getParent() != null) {
                bean = getContext().getParent().getBean(name);
            }
            return bean;
        } catch (BeansException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}