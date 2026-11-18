import java.lang.reflect.*;import java.util.List;import java.util.*;import java.util.concurrent.atomic.AtomicInteger;import java.util.function.Consumer;
import org.eclipse.swt.SWT;import org.eclipse.swt.widgets.*;
public class CheckRefreshQueue {
 public static void main(String[] args)throws Exception{
  Display display=new Display();Shell shell=new Shell(display);
  try{
   for(String name:List.of("com.salutem.theme.RefreshQueue","com.salutem.l10n.RefreshQueue")){
    Class<?> type=Class.forName(name);Constructor<?> create=type.getDeclaredConstructor(Display.class,Consumer.class);create.setAccessible(true);
    List<Control> visited=new ArrayList<>();Object queue=create.newInstance(display,(Consumer<Control>)visited::add);Method request=type.getDeclaredMethod("request",Control.class);request.setAccessible(true);
    Composite root=new Composite(shell,SWT.NONE);Label child=new Label(root,SWT.NONE);
    for(int i=0;i<1000;i++){request.invoke(queue,child);request.invoke(queue,root);}pump(display,250);
    if(visited.size()!=1 || visited.get(0)!=root)throw new AssertionError("Atualizações repetidas/sobrepostas: "+name);
    visited.clear();request.invoke(queue,child);child.dispose();pump(display,200);if(!visited.isEmpty())throw new AssertionError("Controle descartado foi visitado");root.dispose();
   }
   com.salutem.l10n.Startup language=new com.salutem.l10n.Startup();for(int i=0;i<5000;i++)language.translate("Rótulo dinâmico "+i);
   Field cache=language.getClass().getDeclaredField("translatedCache");cache.setAccessible(true);if(((Map<?,?>)cache.get(language)).size()>2048)throw new AssertionError("Cache sem limite");
   if(!language.translate("Text Field").equals("Campo de Texto") || !language.translate("Page 2 of 8").equals("Página 2 de 8"))throw new AssertionError("Evicção prejudicou tradução");
   Locale.setDefault(new Locale("pt","BR"));
   Composite hidden=new Composite(shell,SWT.NONE);hidden.setVisible(false);Label delayed=new Label(hidden,SWT.NONE);delayed.setText("Text Field");
   language.earlyStartup();shell.open();pump(display,200);
   if(!delayed.getText().equals("Text Field"))throw new AssertionError("Percorreu painel oculto");
   hidden.setVisible(true);pump(display,250);
   if(!delayed.getText().equals("Campo de Texto"))throw new AssertionError("Painel exibido não foi traduzido");
   System.out.println("PASS: 2.000 eventos viram uma atualização por módulo; pais/filhos agrupados; controles descartados ignorados; cache limitado e traduções dinâmicas preservadas.");
  }finally{shell.dispose();display.dispose();}
 }
 private static void pump(Display d,int ms)throws Exception{long end=System.currentTimeMillis()+ms;while(System.currentTimeMillis()<end){if(!d.readAndDispatch())Thread.sleep(2);}}
}
