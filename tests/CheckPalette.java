import org.eclipse.swt.widgets.*;import org.eclipse.swt.graphics.*;import org.eclipse.swt.SWT;import org.eclipse.gef.ui.palette.*;import org.eclipse.gef.palette.*;import org.eclipse.draw2d.*;import java.lang.reflect.*;
public class CheckPalette {
 public static void main(String[] args)throws Exception {
  Display d=new Display();Shell s=new Shell(d);s.setSize(400,600);
  try {
   Object platform=org.eclipse.core.internal.runtime.InternalPlatform.getDefault();
   for(Field field:platform.getClass().getDeclaredFields())if(field.getType().getName().equals("org.osgi.framework.BundleContext")){
    field.setAccessible(true);field.set(platform,Proxy.newProxyInstance(CheckPalette.class.getClassLoader(),new Class[]{org.osgi.framework.BundleContext.class},(proxy,method,values)->method.getName().equals("getProperty")?"win32":null));
   }
   new org.eclipse.gef.internal.InternalGEFPlugin(){private final org.eclipse.jface.preference.IPreferenceStore preferences=new org.eclipse.jface.preference.PreferenceStore();public org.eclipse.jface.preference.IPreferenceStore getPreferenceStore(){return preferences;}};
   PaletteViewer v=new PaletteViewer();v.createControl(s);v.getControl().setBounds(0,0,300,500);
   PaletteRoot model=new PaletteRoot();PaletteGroup drawer=new PaletteGroup("Basic Elements");drawer.add(new ToolEntry("Select","Select",null,null,org.eclipse.gef.tools.SelectionTool.class){});model.add(drawer);v.setPaletteRoot(model);
   FigureCanvas canvas=(FigureCanvas)v.getControl();IFigure original=canvas.getContents();
   Object theme=Class.forName("com.salutem.theme.Startup").getConstructor().newInstance();
   for(String name:new String[]{"bg","fg","border","accent"}){Field f=theme.getClass().getDeclaredField(name);f.setAccessible(true);f.set(theme,new Color(d,name.equals("fg")?230:37,name.equals("fg")?233:38,name.equals("fg")?244:56));}
   Method extras=theme.getClass().getDeclaredMethod("extras",Control.class);extras.setAccessible(true);
   extras.invoke(theme,canvas);extras.invoke(theme,canvas);
   if(original.getParent()==null || original.internalGetEventDispatcher()==null)throw new AssertionError("Palette desconectada");
   if(canvas.getContents().getChildren().size()!=1)throw new AssertionError("Conteúdo perdido");
   canvas.getContents().validate();
   Image image=new Image(d,300,500);GC gc=new GC(image);SWTGraphics graphics=new SWTGraphics(gc);canvas.getContents().paint(graphics);graphics.dispose();gc.dispose();image.dispose();
   canvas.notifyListeners(SWT.FocusIn,new Event());
   if(v.getEditPartRegistry().isEmpty())throw new AssertionError("Itens desapareceram");
   System.out.println("PASS: PaletteViewer real mantém conteúdo, itens e dispatcher após duas aplicações; pintura e foco sem exceções.");
  }finally{s.dispose();d.dispose();}
 }
}




