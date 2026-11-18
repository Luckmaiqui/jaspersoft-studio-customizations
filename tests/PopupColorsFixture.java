import org.eclipse.swt.*;import org.eclipse.swt.widgets.*;import org.eclipse.swt.custom.CCombo;import org.eclipse.swt.layout.*;import java.lang.reflect.*;
/** Standalone visual regression fixture; never contacts a server or opens a report. */
public class PopupColorsFixture {
 public static void main(String[] args)throws Exception{
  Display d=new Display();Shell shell=new Shell(d);shell.setText("Verificação de Seleções — JasperStudio");shell.setLayout(new GridLayout(2,false));shell.setSize(650,400);
  Object theme=Class.forName("com.salutem.theme.Startup").getConstructor().newInstance();
  Method apply=theme.getClass().getDeclaredMethod("applyPalette",Display.class,com.salutem.theme.ThemePalette.class);apply.setAccessible(true);apply.invoke(theme,d,com.salutem.theme.ThemePalette.find("com.salutem.jasper.theme.deepblue"));
  Method visit=theme.getClass().getDeclaredMethod("visit",Control.class);visit.setAccessible(true);Method menuTheme=theme.getClass().getDeclaredMethod("menu",Menu.class);menuTheme.setAccessible(true);
  d.setData("org.eclipse.swt.internal.win32.useDarkModeExplorerTheme",Boolean.FALSE);d.setData("org.eclipse.swt.internal.win32.Combo.useDarkTheme",Boolean.FALSE);
  for(int style:new int[]{SWT.READ_ONLY,SWT.NONE}){new Label(shell,SWT.NONE).setText(style==SWT.NONE?"Combo Editável":"Combo de Seleção");Combo c=new Combo(shell,style);c.setItems("Escrever","Ignorar","Escrever Apenas Expressão");c.select(0);c.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));}
  new Label(shell,SWT.NONE).setText("Seleção Personalizada");CCombo custom=new CCombo(shell,SWT.READ_ONLY|SWT.BORDER);custom.setItems(new String[]{"Escrever","Ignorar","Escrever Apenas Expressão"});custom.select(0);custom.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
  new Label(shell,SWT.NONE).setText("Lista");org.eclipse.swt.widgets.List list=new org.eclipse.swt.widgets.List(shell,SWT.SINGLE|SWT.BORDER);list.setItems("Escrever","Ignorar","Escrever Apenas Expressão");list.select(1);list.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
  Button open=new Button(shell,SWT.PUSH);open.setText("Abrir Menu Após Redefinição do Eclipse");GridData width=new GridData(SWT.FILL,SWT.CENTER,true,false);width.horizontalSpan=2;open.setLayoutData(width);
  Menu popup=new Menu(shell,SWT.POP_UP);for(String name:new String[]{"Salvar\tCtrl+S","Salvar Todos\tCtrl+Shift+S","Configurações"}){MenuItem item=new MenuItem(popup,SWT.PUSH);item.setText(name);item.setEnabled(name.equals("Configurações"));}
  open.addListener(SWT.Selection,e->{try{d.setData("org.eclipse.swt.internal.win32.useDarkModeExplorerTheme",Boolean.FALSE);menuTheme.invoke(theme,popup);popup.setLocation(open.toDisplay(0,open.getSize().y));popup.setVisible(true);}catch(Exception error){throw new RuntimeException(error);}});
  visit.invoke(theme,shell);shell.open();if(args.length>0 && args[0].equals("--menu"))open.notifyListeners(SWT.Selection,new Event());while(!shell.isDisposed())if(!d.readAndDispatch())d.sleep();d.dispose();
 }
}

