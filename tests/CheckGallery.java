import org.eclipse.swt.*;import org.eclipse.swt.widgets.*;import org.eclipse.swt.graphics.*;import org.eclipse.nebula.widgets.gallery.*;import java.lang.reflect.*;
public class CheckGallery {public static void main(String[]args)throws Exception{Display d=new Display();Shell shell=new Shell(d);try{
 Object theme=Class.forName("com.salutem.theme.Startup").getConstructor().newInstance();
 Color background=new Color(d,37,38,56),foreground=new Color(d,230,233,244),accent=new Color(d,72,81,118);
 for(String name:new String[]{"bg","fg","border","accent"}){Field f=theme.getClass().getDeclaredField(name);f.setAccessible(true);f.set(theme,name.equals("fg")?foreground:name.equals("accent")?accent:background);}
 Gallery gallery=new Gallery(shell,SWT.V_SCROLL);gallery.setBounds(0,0,500,400);GalleryItem group=new GalleryItem(gallery,SWT.NONE);GalleryItem item=new GalleryItem(group,SWT.NONE);item.setText("Blank A4 Landscape");
 Image preview=new Image(d,60,90);GC pg=new GC(preview);pg.setBackground(d.getSystemColor(SWT.COLOR_WHITE));pg.fillRectangle(0,0,60,90);pg.dispose();item.setImage(preview);gallery.setSelection(new GalleryItem[]{item});
 Method method=theme.getClass().getDeclaredMethod("extras",Control.class);method.setAccessible(true);method.invoke(theme,gallery);method.invoke(theme,gallery);
 DefaultGalleryItemRenderer renderer=(DefaultGalleryItemRenderer)gallery.getItemRenderer();
 if(!gallery.getBackground().equals(background)||!item.getBackground().equals(background))throw new AssertionError("Fundo da galeria");
 if(!renderer.getForegroundColor().equals(foreground)||!renderer.getSelectionBackgroundColor().equals(accent))throw new AssertionError("Contraste da seleção");
 if(gallery.getSelection().length!=1||gallery.getSelection()[0]!=item||item.getImage()!=preview)throw new AssertionError("Modelo ou seleção alterados");
 Image image=new Image(d,200,200);GC gc=new GC(image);renderer.draw(gc,item,0,0,0,180,180);gc.dispose();image.dispose();
 Scale scale=new Scale(shell,SWT.HORIZONTAL);scale.setSelection(55);method.invoke(theme,scale);if(scale.getSelection()!=55||!scale.getBackground().equals(background))throw new AssertionError("Zoom");
 preview.dispose();System.out.println("PASS: Gallery real com fundo escuro, texto claro, seleção destacada, miniatura preservada e zoom funcional.");
}finally{shell.dispose();d.dispose();}}}
