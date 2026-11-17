package com.salutem.order;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import com.jaspersoft.studio.data.noda.NoDataAdapterDescriptor;

public class CheckConnectionSearch {
 public static void main(String[]args)throws Exception {
  org.eclipse.swt.graphics.Rectangle above=ConnectionSearch.place(new org.eclipse.swt.graphics.Rectangle(900,700,100,30),new org.eclipse.swt.graphics.Rectangle(0,0,1000,800),700,450);
  if(above.x!=300 || above.y!=250)throw new AssertionError("Limites inferior/direito");
  org.eclipse.swt.graphics.Rectangle negative=ConnectionSearch.place(new org.eclipse.swt.graphics.Rectangle(-1800,50,100,30),new org.eclipse.swt.graphics.Rectangle(-1920,0,1920,1080),700,450);
  if(negative.x!=-1800 || negative.y!=80)throw new AssertionError("Monitor com coordenadas negativas");
  if(!ConnectionSearch.matches("(014) São José - Hospital","014 sao"))throw new AssertionError("Código/acentos");
  if(ConnectionSearch.matches("(014) São José","001"))throw new AssertionError("Filtro incorreto");
  Display d=new Display();Shell owner=new Shell(d);
  try {
   Menu menu=new Menu(owner,SWT.POP_UP);Object[] selected={null};int[] calls={0};
   Object first=new NoDataAdapterDescriptor(),second=new NoDataAdapterDescriptor();
   MenuItem a=new MenuItem(menu,SWT.CHECK);a.setText("(001) São José");a.setData("da.key",first);
   MenuItem b=new MenuItem(menu,SWT.CHECK);b.setText("(014) São José");b.setData("da.key",second);
   for(MenuItem item:menu.getItems())item.addListener(SWT.Selection,e->{selected[0]=e.widget.getData("da.key");calls[0]++;});
   ToolBar toolbar=new ToolBar(owner,SWT.NONE);toolbar.setBounds(40,30,300,35);
   ToolItem button=new ToolItem(toolbar,SWT.DROP_DOWN);button.setText("Conexão");
   com.jaspersoft.studio.data.widget.DataAdapterAction action=new com.jaspersoft.studio.data.widget.DataAdapterAction(null,new com.jaspersoft.studio.data.storage.ADataAdapterStorage[0]);
   java.lang.reflect.Field listMenu=action.getClass().getDeclaredField("listMenu");listMenu.setAccessible(true);listMenu.set(action,menu);
   button.setData(new org.eclipse.jface.action.ActionContributionItem(action));
   if(ConnectionSearch.findButton(owner,menu)!=button)throw new AssertionError("Não identificou o botão original");
   if(!ConnectionSearch.isAdapterMenu(menu))throw new AssertionError("Menu de conexões não reconhecido");
   Menu other=new Menu(owner,SWT.POP_UP);new MenuItem(other,SWT.PUSH).setText("Outra opção");
   if(ConnectionSearch.isAdapterMenu(other))throw new AssertionError("Interceptou menu não relacionado");
   ConnectionSearch picker=new ConnectionSearch();picker.open(menu);
   Shell popup=last(d,owner);Text search=(Text)popup.getChildren()[0];Table rows=(Table)popup.getChildren()[1];
   org.eclipse.swt.graphics.Rectangle buttonBounds=button.getBounds();org.eclipse.swt.graphics.Point expected=toolbar.toDisplay(buttonBounds.x,buttonBounds.y+buttonBounds.height);
   if(popup.getLocation().x!=expected.x || popup.getLocation().y!=expected.y)throw new AssertionError("Pesquisa distante do botão");
   search.setText("014 sao");if(rows.getItemCount()!=1)throw new AssertionError("Filtro");
   search.notifyListeners(SWT.DefaultSelection,new Event());
   if(selected[0]!=second || calls[0]!=1 || !popup.isDisposed())throw new AssertionError("Identidade/evento da seleção");
   picker.open(menu);popup=last(d,owner);search=(Text)popup.getChildren()[0];rows=(Table)popup.getChildren()[1];
   search.setText("inexistente");if(rows.getItemCount()!=0)throw new AssertionError("Sem resultados");
   search.notifyListeners(SWT.DefaultSelection,new Event());if(calls[0]!=1)throw new AssertionError("Selecionou sem resultado");
   search.setText("");if(rows.getItemCount()!=2)throw new AssertionError("Limpar filtro");
   Event esc=new Event();esc.keyCode=SWT.ESC;search.notifyListeners(SWT.KeyDown,esc);
   if(!popup.isDisposed() || calls[0]!=1)throw new AssertionError("Cancelar mudou conexão");
   picker.open(menu);popup=last(d,owner);menu.dispose();
   if(!popup.isDisposed())throw new AssertionError("Popup órfão");
   Menu nativeMenu=new Menu(owner,SWT.POP_UP);MenuItem nativeItem=new MenuItem(nativeMenu,SWT.CHECK);
   nativeItem.setData("da.key",first);nativeItem.setText("(001) Conexão do menu nativo");
   picker.install(d);owner.open();nativeMenu.setLocation(owner.toDisplay(10,10));
   d.asyncExec(()->nativeMenu.setVisible(true));
   long timeout=System.currentTimeMillis()+3000;
   while(d.getShells().length<2 && System.currentTimeMillis()<timeout)if(!d.readAndDispatch())try{Thread.sleep(5);}catch(InterruptedException e){throw new AssertionError(e);}
   popup=last(d,owner);
   if(nativeMenu.isVisible() || popup.getChildren().length!=3)throw new AssertionError("Menu nativo não foi substituído pela pesquisa");
   popup.dispose();nativeMenu.dispose();
   System.out.println("PASS: pesquisa por código/nome/acentos; evento e identidade; filtro vazio; Esc; descarte; menus não relacionados.");
  }finally{owner.dispose();d.dispose();}
 }
 private static Shell last(Display d,Shell owner){for(Shell s:d.getShells())if(s!=owner)return s;throw new AssertionError("Popup ausente");}
}
