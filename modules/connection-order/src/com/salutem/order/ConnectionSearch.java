package com.salutem.order;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import com.jaspersoft.studio.data.DataAdapterDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/** Presents the original adapter menu items in a searchable popup; never edits storage. */
public final class ConnectionSearch {
 private Shell popup;
 public void install(Display display) {
  display.addFilter(SWT.Show,event->{
   if(event.widget instanceof Menu) {
    Menu menu=(Menu)event.widget;
    // Run after the application's Show listeners have finished populating the menu.
    display.asyncExec(()->{
     if(!menu.isDisposed() && menu.isVisible() && isAdapterMenu(menu))open(menu);
    });
   }
  });
 }
 static boolean isAdapterMenu(Menu menu) {
  for(MenuItem item:menu.getItems())if(item.getData("da.key") instanceof DataAdapterDescriptor)return true;
  return false;
 }
 static String normalize(String value) {
  return Normalizer.normalize(value,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT);
 }
 static boolean matches(String label,String query) {
  String value=normalize(label);
  for(String term:normalize(query).strip().split("\\s+"))if(!value.contains(term))return false;
  return true;
 }
 void open(Menu menu) {
  if(menu.isDisposed())return;
  if(popup!=null && !popup.isDisposed())popup.dispose();
  List<MenuItem> source=new ArrayList<>();
  for(MenuItem item:menu.getItems())if((item.getStyle()&SWT.SEPARATOR)==0 && item.getMenu()==null)source.add(item);
  if(source.isEmpty())return;
  Display display=menu.getDisplay();Shell owner=menu.getShell();
  Point position=display.getCursorLocation();
  Control focus=display.getFocusControl();
  if(focus!=null && !focus.isDisposed()) {
   Rectangle bounds=focus.getBounds();Point anchor=focus.toDisplay(0,bounds.height);
   // Keyboard activation has no pointer anchor over the focused control.
   Point origin=focus.toDisplay(0,0);
   if(!new Rectangle(origin.x,origin.y,bounds.width,bounds.height).contains(position))position=anchor;
  }
  menu.setVisible(false);
  Shell window=new Shell(owner,SWT.ON_TOP|SWT.TOOL|SWT.RESIZE);
  popup=window;window.setText(pt()?"Pesquisar conexão":"Search connections");
  window.setLayout(new GridLayout(1,false));
  window.setBackground(owner.getBackground());window.setForeground(owner.getForeground());
  Text search=new Text(window,SWT.SEARCH|SWT.ICON_SEARCH|SWT.ICON_CANCEL|SWT.BORDER);
  search.setMessage(pt()?"Pesquisar por código ou nome…":"Search by code or name…");
  search.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
  Table table=new Table(window,SWT.SINGLE|SWT.FULL_SELECTION|SWT.V_SCROLL);
  table.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
  table.setBackground(owner.getBackground());table.setForeground(owner.getForeground());
  search.setBackground(table.getBackground());search.setForeground(table.getForeground());
  Label status=new Label(window,SWT.NONE);status.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
  status.setBackground(owner.getBackground());status.setForeground(owner.getForeground());
  Runnable filter=()->{
   table.setRedraw(false);
   try {
    table.removeAll();
    for(MenuItem original:source)if(!original.isDisposed() && matches(original.getText(),search.getText())) {
     TableItem row=new TableItem(table,SWT.NONE);row.setText(original.getText());row.setData(original);
     if(original.getImage()!=null)row.setImage(original.getImage());
     if(!original.getEnabled())row.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
    }
    if(table.getItemCount()>0)table.setSelection(0);
    int count=table.getItemCount();status.setText(count==0?(pt()?"Nenhuma conexão encontrada":"No matching connections"):
     count+(pt()?" opções — Enter: selecionar · Esc: cancelar":" choices — Enter: select · Esc: cancel"));
   }finally{table.setRedraw(true);}
  };
  Runnable accept=()->{
   if(window.isDisposed() || table.getSelectionCount()==0)return;
   MenuItem original=(MenuItem)table.getSelection()[0].getData();
   if(original.isDisposed() || !original.getEnabled())return;
   // Keep the native item's listeners and descriptor identity, including default/no-data options.
   window.dispose();
   if(!original.isDisposed())original.notifyListeners(SWT.Selection,new Event());
  };
  search.addModifyListener(e->filter.run());
  search.addListener(SWT.DefaultSelection,e->{if(e.detail!=SWT.ICON_CANCEL)accept.run();});
  Listener keys=e->{
   if(e.keyCode==SWT.ESC){e.doit=false;window.dispose();}
   else if(e.widget==search && (e.keyCode==SWT.ARROW_DOWN || e.keyCode==SWT.ARROW_UP)) {
    e.doit=false;int size=table.getItemCount();
    if(size>0){int index=table.getSelectionIndex()+(e.keyCode==SWT.ARROW_DOWN?1:-1);table.setSelection(Math.max(0,Math.min(size-1,index)));table.showSelection();}
   }else if(e.widget==table && (e.keyCode==SWT.CR || e.keyCode==SWT.KEYPAD_CR)){e.doit=false;accept.run();}
  };
  search.addListener(SWT.KeyDown,keys);table.addListener(SWT.KeyDown,keys);
  table.addListener(SWT.MouseUp,e->{if(e.button==1 && table.getItem(new Point(e.x,e.y))!=null)accept.run();});
  window.addListener(SWT.Deactivate,e->display.asyncExec(()->{if(!window.isDisposed() && display.getActiveShell()!=window)window.dispose();}));
  Listener originalDisposed=e->{if(!window.isDisposed())window.dispose();};
  menu.addListener(SWT.Dispose,originalDisposed);
  window.addListener(SWT.Dispose,e->{if(!menu.isDisposed())menu.removeListener(SWT.Dispose,originalDisposed);if(popup==window)popup=null;});
  filter.run();Rectangle area=owner.getMonitor().getClientArea();
  int width=Math.min(700,area.width),height=Math.min(450,area.height);
  window.setBounds(Math.max(area.x,Math.min(position.x,area.x+area.width-width)),Math.max(area.y,Math.min(position.y,area.y+area.height-height)),width,height);
  window.open();search.setFocus();
 }
 private boolean pt(){return Locale.getDefault().getLanguage().equals("pt");}
}
