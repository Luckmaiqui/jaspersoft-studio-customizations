import org.eclipse.swt.*;import org.eclipse.swt.widgets.*;import org.eclipse.swt.graphics.*;import org.eclipse.swt.layout.GridLayout;import org.eclipse.jface.wizard.ProgressMonitorPart;import org.eclipse.nebula.widgets.gallery.*;import com.salutem.theme.ThemePalette;import java.lang.reflect.*;
public class CheckThemeSwitch {
 public static void main(String[]args)throws Exception{
  Display d=new Display();Shell shell=new Shell(d);
  try{
   Object theme=Class.forName("com.salutem.theme.Startup").getConstructor().newInstance();
   Method apply=theme.getClass().getDeclaredMethod("applyPalette",Display.class,ThemePalette.class);apply.setAccessible(true);
   Method extras=theme.getClass().getDeclaredMethod("extras",Control.class);extras.setAccessible(true);
   Gallery gallery=new Gallery(shell,SWT.V_SCROLL);GalleryItem group=new GalleryItem(gallery,SWT.NONE);GalleryItem item=new GalleryItem(group,SWT.NONE);item.setText("Template");gallery.setSelection(new GalleryItem[]{item});
   ProgressMonitorPart progress=new ProgressMonitorPart(shell,new GridLayout());
   int count=0;
   for(ThemePalette p:ThemePalette.all()){
    apply.invoke(theme,d,p);extras.invoke(theme,gallery);extras.invoke(theme,progress);
    RGB expected=new RGB((p.background>>16)&255,(p.background>>8)&255,p.background&255);
    if(!gallery.getBackground().getRGB().equals(expected)||!progress.getBackground().getRGB().equals(expected))throw new AssertionError(p.id);
    DefaultGalleryItemRenderer renderer=(DefaultGalleryItemRenderer)gallery.getItemRenderer();
    RGB selected=renderer.getSelectionBackgroundColor().getRGB();
    if(selected.red!=((p.accent>>16)&255)||selected.green!=((p.accent>>8)&255)||selected.blue!=(p.accent&255))throw new AssertionError("Seleção: "+p.id);
    Color disabled=(Color)d.getData("org.eclipse.swt.internal.win32.Label.disabledForegroundColor");
    if(disabled.getRed()!=((p.muted>>16)&255)||disabled.getGreen()!=((p.muted>>8)&255)||disabled.getBlue()!=(p.muted&255))throw new AssertionError("Rótulo desabilitado: "+p.id);
    if(gallery.getSelection()[0]!=item)throw new AssertionError("Perdeu a seleção");count++;
   }
   if(count!=7)throw new AssertionError("Quantidade de temas");
   Field pool=theme.getClass().getDeclaredField("colorPool");pool.setAccessible(true);int colors=((java.util.Map<?,?>)pool.get(theme)).size();
   for(int repeat=0;repeat<10;repeat++)for(ThemePalette p:ThemePalette.all())apply.invoke(theme,d,p);
   if(((java.util.Map<?,?>)pool.get(theme)).size()!=colors)throw new AssertionError("Cores acumuladas ao alternar temas");
   System.out.println("PASS: sete paletas aplicadas a Gallery e ProgressMonitorPart; seleção e rótulos desabilitados atualizados.");
  }finally{shell.dispose();d.dispose();}
 }
}
