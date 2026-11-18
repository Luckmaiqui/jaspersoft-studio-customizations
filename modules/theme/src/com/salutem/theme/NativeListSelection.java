package com.salutem.theme;
import java.util.function.Supplier;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.win32.*;
import org.eclipse.swt.widgets.*;

/** Paint-only Win32 list selection fix for the pinned SWT 3.124 Windows bundle.
 * Native processing still owns selection, scrolling, accessibility and notifications.
 */
final class NativeListSelection {
 private static final int WM_NCDESTROY=0x0082; // Win32: final native-window destruction notification.
 private final Control owner;private final Display display;private final long handle,previous;private final Callback callback;
 private final Supplier<Color> background,foreground;private boolean painting,released,reported;
 NativeListSelection(Control owner,long handle,Supplier<Color> background,Supplier<Color> foreground){
  this.owner=owner;this.display=owner.getDisplay();this.handle=handle;this.background=background;this.foreground=foreground;
  callback=new Callback(this,"windowProc",4);
  if(callback.getAddress()==0){callback.dispose();throw new IllegalStateException("Native selection callback unavailable");}
  previous=OS.SetWindowLongPtr(handle,OS.GWLP_WNDPROC,callback.getAddress());
  if(previous==0){callback.dispose();throw new IllegalStateException("Native list window unavailable");}
  owner.addListener(SWT.Dispose,e->close());
 }
 boolean matches(long candidate){return handle==candidate && !released;}
 @SuppressWarnings("unused") long windowProc(long hwnd,long message,long wParam,long lParam){
  long result=OS.CallWindowProc(previous,hwnd,(int)message,wParam,lParam);
  if(message==WM_NCDESTROY){releaseLater();return result;}
  boolean repaint=message==OS.WM_PAINT || message==OS.WM_PRINTCLIENT || message==OS.WM_MOUSEMOVE || message==OS.WM_LBUTTONDOWN || message==OS.WM_KEYDOWN || message==OS.WM_VSCROLL || message==OS.WM_HSCROLL || message==OS.LB_SETCURSEL || message==OS.LB_SETSEL;
  if(repaint && !painting && !released && !owner.isDisposed()){
   painting=true;
   try{paint(message==OS.WM_PRINTCLIENT?wParam:0);}catch(Throwable error){if(!reported){reported=true;System.err.println("Salutem list selection paint: "+error.getClass().getSimpleName());}}finally{painting=false;}
  }
  return result;
 }
 private void paint(long suppliedDC){
  Color bg=background.get(),fg=foreground.get();if(bg==null || fg==null || bg.isDisposed() || fg.isDisposed())return;
  RECT client=new RECT();if(!OS.GetClientRect(handle,client))return;
  int count=(int)OS.SendMessage(handle,OS.LB_GETCOUNT,0,0),top=(int)OS.SendMessage(handle,OS.LB_GETTOPINDEX,0,0),current=(int)OS.SendMessage(handle,OS.LB_GETCURSEL,0,0);
  SCROLLINFO scroll=new SCROLLINFO();scroll.cbSize=SCROLLINFO.sizeof;scroll.fMask=OS.SIF_POS;OS.GetScrollInfo(handle,OS.SB_HORZ,scroll);
  boolean multiple=owner instanceof org.eclipse.swt.widgets.List && (owner.getStyle()&SWT.MULTI)!=0;
  long dc=suppliedDC!=0?suppliedDC:OS.GetDC(handle);if(dc==0)return;
  int saved=OS.SaveDC(dc);long brush=OS.CreateSolidBrush(bg.handle);
  try{
   long font=OS.SendMessage(handle,OS.WM_GETFONT,0,0);if(font!=0)OS.SelectObject(dc,font);
   OS.SetTextColor(dc,fg.handle);OS.SetBkMode(dc,OS.TRANSPARENT);
   for(int index=Math.max(0,top);index<count;index++){
    RECT row=new RECT();if(OS.SendMessage(handle,OS.LB_GETITEMRECT,index,row)==OS.LB_ERR)break;if(row.top>=client.bottom)break;
    boolean selected=multiple?OS.SendMessage(handle,OS.LB_GETSEL,index,0)>0:index==current;
    if(!selected || row.bottom<=client.top)continue;
    String text=owner instanceof Combo?((Combo)owner).getItem(index):((org.eclipse.swt.widgets.List)owner).getItem(index);
    OS.FillRect(dc,row,brush);row.left+=2-scroll.nPos;row.right-=2;
    int flags=OS.DT_SINGLELINE|OS.DT_VCENTER|OS.DT_NOPREFIX|OS.DT_EXPANDTABS;
    if((owner.getStyle()&SWT.RIGHT_TO_LEFT)!=0)flags|=OS.DT_RIGHT|OS.DT_RTLREADING;
    char[] chars=text.toCharArray();OS.DrawText(dc,chars,chars.length,row,flags);
   }
  }finally{OS.DeleteObject(brush);OS.RestoreDC(dc,saved);if(suppliedDC==0)OS.ReleaseDC(handle,dc);}
 }
 void close(){
  if(released)return;
  long current=OS.GetWindowLongPtr(handle,OS.GWLP_WNDPROC);
  // Zero means the native window is already gone. Preserve later extension hooks.
  if(current!=0 && current!=callback.getAddress())return;
  if(current==callback.getAddress())OS.SetWindowLongPtr(handle,OS.GWLP_WNDPROC,previous);
  releaseLater();
 }
 private void releaseLater(){
  if(released)return;released=true;
  try{if(!display.isDisposed())display.asyncExec(callback::dispose);}catch(org.eclipse.swt.SWTException ignored){ }
 }
}
