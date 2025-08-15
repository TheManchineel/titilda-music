package org.titilda.music.ssr;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;

/**
 * Simple utility class for handling Thymeleaf template processing.
 * Provides a centralized way to configure and render templates.
 */
public class TemplateManager {
    private static final TemplateEngine templateEngine;
    
    static {
        templateEngine = new TemplateEngine();
        
        // Configure template resolver to look for templates in classpath
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false); // Set to true in production
        
        templateEngine.setTemplateResolver(templateResolver);
    }
    
    /**
     * Renders a template with the given variables.
     * 
     * @param templateName the name of the template (without .html extension)
     * @param variables map of variables to pass to the template
     * @return the rendered HTML as a string
     */
    public static String render(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }
        return templateEngine.process(templateName, context);
    }
    
    /**
     * Renders a template without any variables.
     * 
     * @param templateName the name of the template (without .html extension)
     * @return the rendered HTML as a string
     */
    public static String render(String templateName) {
        return render(templateName, null);
    }
}
