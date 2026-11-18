import org.eclipse.swt.*;import org.eclipse.swt.widgets.*;import org.eclipse.swt.custom.CCombo;import org.eclipse.swt.graphics.Color;import org.eclipse.swt.internal.Callback;import org.eclipse.swt.internal.win32.*;import java.lang.reflect.*;
public class CheckNativeSelection {
 public static void main(String[]args)throws Exception{
  Display display=new Display();Shell shell=new Shell(display);shell.setSize(460,260);
  try{
   Object theme=Class.forName("com.salutem.theme.Startup").getConstructor().newInstance();
   Method apply=theme.getClass().getDeclaredMethod("applyPalette",Display.class,com.salutem.theme.ThemePalette.class);apply.setAccessible(true);apply.invoke(theme,display,com.salutem.theme.ThemePalette.find("com.salutem.jasper.theme.deepblue"));
   Method nativeCombo=theme.getClass().getDeclaredMethod("nativeCombo",Control.class);nativeCombo.setAccessible(true);
   org.eclipse.swt.widgets.List list=new org.eclipse.swt.widgets.List(shell,SWT.MULTI);list.setBounds(10,10,400,80);list.setItems("Escrever","Ignorar","Escrever Apenas Expressão");list.select(new int[]{0,2});
   nativeCombo.invoke(theme,list);Object painter=list.getData("salutem.selectionPainter");nativeCombo.invoke(theme,list);if(painter!=list.getData("salutem.selectionPainter"))throw new AssertionError("Instalou dois hooks");
   shell.open();pump(display);OS.InvalidateRect(list.handle,null,true);OS.UpdateWindow(list.handle);
   Field accent=theme.getClass().getDeclaredField("accent");accent.setAccessible(true);int expected=((Color)accent.get(theme)).handle;
   RECT row=new RECT();OS.SendMessage(list.handle,OS.LB_GETITEMRECT,0,row);long dc=OS.GetDC(list.handle);try{if(OS.GetPixel(dc,350,(row.top+row.bottom)/2)!=expected)throw new AssertionError("Seleção não usa destaque do tema");}finally{OS.ReleaseDC(list.handle,dc);}
   if(list.getSelectionCount()!=2 || list.getSelectionIndices()[1]!=2)throw new AssertionError("Seleção múltipla alterada");
   for(com.salutem.theme.ThemePalette palette:com.salutem.theme.ThemePalette.all()){
    apply.invoke(theme,display,palette);OS.InvalidateRect(list.handle,null,true);OS.UpdateWindow(list.handle);
    dc=OS.GetDC(list.handle);try{if(OS.GetPixel(dc,350,(row.top+row.bottom)/2)!=((Color)accent.get(theme)).handle)throw new AssertionError("Destaque incorreto: "+palette.id);}finally{OS.ReleaseDC(list.handle,dc);}
   }
   apply.invoke(theme,display,com.salutem.theme.ThemePalette.find("com.salutem.jasper.theme.deepblue"));
   Combo combo=new Combo(shell,SWT.READ_ONLY);combo.setBounds(10,110,400,25);combo.setItems("Escrever","Ignorar","Escrever Apenas Expressão");combo.select(0);nativeCombo.invoke(theme,combo);
   combo.setListVisible(true);pump(display);COMBOBOXINFO info=new COMBOBOXINFO();info.cbSize=COMBOBOXINFO.sizeof;OS.GetComboBoxInfo(combo.handle,info);OS.InvalidateRect(info.hwndList,null,true);OS.UpdateWindow(info.hwndList);
   dc=OS.GetDC(info.hwndList);try{if(OS.GetPixel(dc,350,5)!=expected)throw new AssertionError("Destaque do combo continua branco");}finally{OS.ReleaseDC(info.hwndList,dc);}combo.setListVisible(false);
   if(combo.getSelectionIndex()!=0 || !combo.getText().equals("Escrever"))throw new AssertionError("Combo alterou o valor");
   // A popup opened programmatically used to pass while the refresh at click
   // time immediately dismissed it. Exercise the same repeated theme requests
   // that MouseDown/FocusIn/the refresh queue make in the running application.
   for(int style:new int[]{SWT.READ_ONLY,SWT.NONE}){
    Combo opening=new Combo(shell,style);opening.setBounds(10,200,400,25);opening.setItems("A","B","C");opening.select(1);nativeCombo.invoke(theme,opening);
    for(int attempt=0;attempt<5;attempt++){
     nativeCombo.invoke(theme,opening);opening.setListVisible(true);pump(display);
     for(int refresh=0;refresh<10;refresh++){nativeCombo.invoke(theme,opening);pump(display);if(!opening.getListVisible())throw new AssertionError("Atualização do tema fechou o combo: estilo="+style+", tentativa="+attempt);}
     opening.setListVisible(false);
    }
    if(opening.getSelectionIndex()!=1 || !opening.getItem(1).equals("B"))throw new AssertionError("Abertura repetida alterou a opção");opening.dispose();
   }
   CCombo custom=new CCombo(shell,SWT.READ_ONLY);custom.setBounds(10,150,400,25);custom.setItems(new String[]{"Escrever","Ignorar"});custom.select(1);
   Method extras=theme.getClass().getDeclaredMethod("extras",Control.class);extras.setAccessible(true);extras.invoke(theme,custom);Field popup=CCombo.class.getDeclaredField("list");popup.setAccessible(true);
   if(((Control)popup.get(custom)).getData("salutem.selectionPainter")==null || custom.getSelectionIndex()!=1)throw new AssertionError("CCombo não coberto ou seleção alterada");
   Field callback=painter.getClass().getDeclaredField("callback");callback.setAccessible(true);Callback bridge=(Callback)callback.get(painter);list.dispose();pump(display);if(bridge.getAddress()!=0)throw new AssertionError("Callback não liberado");
   for(int i=0;i<100;i++){Combo temporary=new Combo(shell,SWT.READ_ONLY);temporary.setItems("A","B");temporary.select(1);nativeCombo.invoke(theme,temporary);temporary.dispose();}pump(display);
   System.out.println("PASS: combos editáveis e somente seleção permanecem abertos após 100 atualizações; pixels do destaque, seleção múltipla, CCombo e valores preservados; hook único; liberação e 100 criações/descartes sem falhas.");
  }finally{shell.dispose();pump(display);display.dispose();}
 }
 private static void pump(Display d)throws Exception{long end=System.currentTimeMillis()+80;while(System.currentTimeMillis()<end){if(!d.readAndDispatch())Thread.sleep(2);}}
}
