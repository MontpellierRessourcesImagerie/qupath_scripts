import groovy.lang.GroovyShell;
import groovy.lang.Binding;


Binding binding = new Binding();
binding.setVariable("settings_name", "sd-nuclei");
def shell = new GroovyShell(this.class.classLoader, binding);
shell.evaluate(QP.getProject().getScripts().get("launch-cpsd"));