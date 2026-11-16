import org.eclipse.swt.*;import org.eclipse.swt.widgets.*;import org.eclipse.swt.graphics.*;import java.lang.reflect.*;import com.jaspersoft.studio.server.publish.wizard.page.DatasourceSelectionComposite;
public class CheckPublishing {public static void main(String[]args)throws Exception{Display d=new Display();Shell shell=new Shell(d);try{
 Object theme=Class.forName("com.salutem.theme.Startup").getConstructor().newInstance();Color bg=new Color(d,37,38,56),fg=new Color(d,230,233,244);
 for(String name:new String[]{"bg","fg","border","accent"}){Field f=theme.getClass().getDeclaredField(name);f.setAccessible(true);f.set(theme,name.equals("fg")?fg:bg);}
 TabFolder tabs=new TabFolder(shell,SWT.NONE);TabItem tab=new TabItem(tabs,SWT.NONE);DatasourceSelectionComposite panel=new DatasourceSelectionComposite(tabs,SWT.NONE,false,new String[0]);tab.setControl(panel);
 Method method=theme.getClass().getDeclaredMethod("extras",Control.class);method.setAccessible(true);method.invoke(theme,tabs);check(tabs,bg,fg);
 System.out.println("PASS: componente real de fonte de dados de publicação, abas e filhos com fundo azul e texto claro.");
}finally{shell.dispose();d.dispose();}}
static void check(Control c,Color bg,Color fg){if(!c.getBackground().equals(bg)||!c.getForeground().equals(fg))throw new AssertionError("Cor incorreta: "+c.getClass());if(c instanceof Composite)for(Control child:((Composite)c).getChildren())check(child,bg,fg);}}
