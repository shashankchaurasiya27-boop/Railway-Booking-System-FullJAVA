package com.railway.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Utility class to provide access to Spring ApplicationContext from non-Spring
 * managed classes.
 * This allows Swing UI components to retrieve Spring beans.
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * Get a Spring bean by class type
     * 
     * @param beanClass The class of the bean to retrieve
     * @return The Spring-managed bean instance
     */
    public static <T> T getBean(Class<T> beanClass) {
        if (context == null) {
            throw new IllegalStateException("Spring ApplicationContext not initialized");
        }
        return context.getBean(beanClass);
    }

    /**
     * Get a Spring bean by name
     * 
     * @param beanName The name of the bean
     * @return The Spring-managed bean instance
     */
    public static Object getBean(String beanName) {
        if (context == null) {
            throw new IllegalStateException("Spring ApplicationContext not initialized");
        }
        return context.getBean(beanName);
    }

    /**
     * Check if Spring context is initialized
     * 
     * @return true if context is available
     */
    public static boolean isContextInitialized() {
        return context != null;
    }
}
