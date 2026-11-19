package com.salutem.theme;
import java.lang.reflect.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;

/**
 * Cor personalizada por bloco × pintura das paletas Salutem (Startup).
 * Teste de componentes SWT; não substitui a conferência no Studio completo.
 */
public class CheckCustomColors {
 static int failures;
 static void check(boolean ok,String message){System.out.println((ok?"PASS: ":"FAIL: ")+message);if(!ok)failures++;}

 public static void main(String[] args)throws Exception{
  Display display=new Display();Shell shell=new Shell(display);
  Color custom=new Color(display,20,150,90),ancestor=new Color(display,120,40,160),fg=new Color(display,230,237,243);
  Color palette=new Color(display,13,17,23),otherPalette=new Color(display,22,27,34);
  String[] stored={"view|Visão|14965A\nstack|Pilha|7828A0"};
  CustomColors.stored=()->stored[0];
  try{
   Composite stack=new Composite(shell,SWT.NONE);
   Composite part=new Composite(stack,SWT.NONE);part.setData(CustomColors.OWNER,BasicFactoryImpl.eINSTANCE.createPart());
   ToolBar toolbar=new ToolBar(part,SWT.NONE);CLabel label=new CLabel(part,SWT.NONE);label.setText("Bloco de teste");
   Composite panel=new Composite(part,SWT.NONE);CLabel panelLabel=new CLabel(panel,SWT.NONE);Scale scale=new Scale(panel,SWT.NONE);
   CTabFolder emptyTabs=new CTabFolder(part,SWT.NONE);
   Table table=new Table(part,SWT.NONE);Text text=new Text(part,SWT.NONE);text.setText("Conteudo preservado");
   RGB tableBefore=table.getBackground().getRGB(),textBefore=text.getBackground().getRGB();
   Control[] chosen={toolbar,label,panel,panelLabel,emptyTabs};

   // Limite MPart: a cor da pilha ancestral não entra na visão, nas duas ordens.
   for(boolean partFirst:new boolean[]{true,false}){
    Set<Control> painted=Collections.newSetFromMap(new IdentityHashMap<>());
    if(partFirst){CustomColors.paint(part,"view",custom,part,painted);CustomColors.paint(stack,"stack",ancestor,stack,painted);}
    else{CustomColors.paint(stack,"stack",ancestor,stack,painted);CustomColors.paint(part,"view",custom,part,painted);}
    check(all(chosen,custom)&&custom.equals(emptyTabs.getSelectionBackground()),"limite MPart protege a visão da cor do ancestral ("+(partFirst?"visão antes":"pilha antes")+")");
   }
   check(stack.getBackground().equals(ancestor),"pilha ancestral recebe a própria cor");
   check(custom.equals(CustomColors.background(toolbar))&&CustomColors.background(table)==null&&CustomColors.background(text)==null,"consulta central: só controles estruturais do bloco têm cor escolhida");

   // Rotina Salutem com fundo 13,17,23 não retira a escolha, nas duas ordens e em várias passagens.
   Object startup=Class.forName("com.salutem.theme.Startup").getConstructor().newInstance();
   setPalette(startup,palette,fg);
   Method extras=method(startup,"extras"),visit=method(startup,"visit"),publish=method(startup,"publishColors"),property=method(startup,"propertyColors");
   boolean stable=true;
   for(int pass=0;pass<6;pass++){
    Set<Control> painted=Collections.newSetFromMap(new IdentityHashMap<>());
    if(pass%2==0){CustomColors.paint(part,"view",custom,part,painted);salutem(startup,extras,visit,publish,property,part,panel,toolbar,label,panelLabel,scale,emptyTabs);}
    else{salutem(startup,extras,visit,publish,property,part,panel,toolbar,label,panelLabel,scale,emptyTabs);CustomColors.paint(part,"view",custom,part,painted);}
    // Depois de cada rotina isolada, não só no fim da passagem.
    salutem(startup,extras,visit,publish,property,part,panel,toolbar,label,panelLabel,scale,emptyTabs);
    if(!all(chosen,custom)||!custom.equals(emptyTabs.getSelectionBackground())){stable=false;System.out.println("  passagem "+pass+": toolbar="+toolbar.getBackground().getRGB()+" clabel="+label.getBackground().getRGB());}
   }
   check(stable,"ToolBar, CLabel, painel e abas vazias mantêm RGB 20,150,90 após extras, visit, publishColors e propertyColors (6 passagens, ordens alternadas)");
   check(label.getForeground().equals(fg),"texto do CLabel continua com a cor da paleta");
   check(table.getBackground().getRGB().equals(tableBefore)&&text.getBackground().getRGB().equals(textBefore)&&text.getText().equals("Conteudo preservado"),"Table e Text sem pintura de contêiner e texto preservado");

   // Remoção: a paleta volta a valer na mesma hora, mesmo antes da passagem de restauração.
   stored[0]="stack|Pilha|7828A0";
   extras.invoke(startup,toolbar);extras.invoke(startup,label);
   check(toolbar.getBackground().equals(palette)&&label.getBackground().equals(palette),"cor removida: rotina Salutem devolve o fundo da paleta sem esperar a restauração");
   Set<Control> painted=Collections.newSetFromMap(new IdentityHashMap<>());
   CustomColors.paint(stack,"stack",ancestor,stack,painted);CustomColors.restore(shell,painted);
   boolean cleared=true;for(Control c:chosen)cleared&=CustomColors.background(c)==null;
   check(cleared&&ancestor.equals(CustomColors.background(stack)),"restauração limpa o estado da visão removida e mantém o da pilha");
   setPalette(startup,otherPalette,fg);
   visit.invoke(startup,part);
   check(toolbar.getBackground().equals(otherPalette)&&label.getBackground().equals(otherPalette)&&panelLabel.getBackground().equals(otherPalette),"após remover, troca de paleta aplica o fundo da nova paleta");
   stored[0]="";
   check(CustomColors.background(stack)==null,"restaurar tudo: nenhuma escolha ativa");

   // Seletor: hexadecimal e diálogo.
   check(CustomColors.parse(ColorPickerDialog.expand("#abc")).equals(new RGB(170,187,204))&&CustomColors.parse("GGGGGG")==null,"hexadecimal curto expandido e inválido recusado");
   display.timerExec(800,()->{for(Shell candidate:display.getShells())if(candidate!=shell&&!candidate.isDisposed())candidate.close();});
   check(new ColorPickerDialog(shell,new RGB(20,150,90)).open()==null,"diálogo abre e fecha sem aplicar cor");
  }finally{
   shell.dispose();for(Color c:new Color[]{custom,ancestor,fg,palette,otherPalette})c.dispose();display.dispose();
  }
  System.out.println("RESULT: "+failures+" falha(s)");
  if(failures>0)System.exit(2);
 }

 static boolean all(Control[] controls,Color color){for(Control c:controls)if(!c.getBackground().equals(color))return false;return true;}
 static Method method(Object startup,String name)throws Exception{Method m=startup.getClass().getDeclaredMethod(name,Control.class);m.setAccessible(true);return m;}
 static void setPalette(Object startup,Color background,Color foreground)throws Exception{
  for(String name:new String[]{"fg","bg","border","accent","muted"}){Field field=startup.getClass().getDeclaredField(name);field.setAccessible(true);field.set(startup,name.equals("fg")?foreground:background);}
 }
 /** Every Salutem routine that paints backgrounds of these controls. */
 static void salutem(Object startup,Method extras,Method visit,Method publish,Method property,Composite part,Composite panel,Control... controls)throws Exception{
  for(Control c:controls)extras.invoke(startup,c);
  visit.invoke(startup,part);publish.invoke(startup,panel);property.invoke(startup,panel);property.invoke(startup,controls[1]);
 }
}
