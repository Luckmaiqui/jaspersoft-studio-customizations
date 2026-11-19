package com.salutem.theme;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * Screen eyedropper: hides the owner, freezes a picture of every monitor and lets the
 * user click any pixel. A magnifier with the hex code follows the mouse; Esc or the
 * right button cancels. Returns null when cancelled.
 */
final class EyeDropper {
 private static final int CELLS=15,ZOOM=9,LOUPE=CELLS*ZOOM,GAP=22;
 private final Display display;private final Rectangle screen;
 private Image shot;private ImageData pixels;private Shell overlay;
 private Point mouse=new Point(-1,-1);private RGB result;private boolean done;

 private EyeDropper(Display display){this.display=display;screen=display.getBounds();}

 static RGB pick(Shell owner){
  EyeDropper dropper=new EyeDropper(owner.getDisplay());
  owner.setVisible(false);
  try{dropper.settle(220);return dropper.run();}
  finally{if(!owner.isDisposed()){owner.setVisible(true);owner.forceActive();}}
 }

 /** Lets the windows behind the hidden dialog repaint before the screen is copied. */
 private void settle(long millis){
  long end=System.currentTimeMillis()+millis;
  while(System.currentTimeMillis()<end){if(!display.readAndDispatch())try{Thread.sleep(10);}catch(InterruptedException e){Thread.currentThread().interrupt();return;}}
 }

 private RGB run(){
  shot=new Image(display,screen.width,screen.height);
  GC copy=new GC(display);
  try{copy.copyArea(shot,screen.x,screen.y);}finally{copy.dispose();}
  pixels=shot.getImageData();
  Cursor cross=new Cursor(display,SWT.CURSOR_CROSS);
  overlay=new Shell(display,SWT.NO_TRIM|SWT.ON_TOP);
  overlay.setBounds(screen);overlay.setCursor(cross);
  overlay.addListener(SWT.Paint,this::paint);
  overlay.addListener(SWT.MouseMove,e->move(new Point(e.x,e.y)));
  overlay.addListener(SWT.MouseDown,e->{if(e.button==1)result=at(e.x,e.y);done=true;});
  overlay.addListener(SWT.KeyDown,e->{if(e.keyCode==SWT.ESC)done=true;});
  overlay.addListener(SWT.Deactivate,e->done=true);
  overlay.open();overlay.forceActive();
  Point start=display.getCursorLocation();move(new Point(start.x-screen.x,start.y-screen.y));
  try{while(!done && !overlay.isDisposed())if(!display.readAndDispatch())display.sleep();}
  finally{if(!overlay.isDisposed())overlay.dispose();shot.dispose();cross.dispose();}
  return result;
 }

 private RGB at(int x,int y){
  int px=Math.max(0,Math.min(pixels.width-1,x)),py=Math.max(0,Math.min(pixels.height-1,y));
  return pixels.palette.getRGB(pixels.getPixel(px,py));
 }

 private Rectangle loupe(Point p){
  int w=LOUPE+2,h=LOUPE+30;
  int x=p.x+GAP,y=p.y+GAP;
  if(x+w>screen.width)x=p.x-GAP-w;
  if(y+h>screen.height)y=p.y-GAP-h;
  return new Rectangle(Math.max(0,x),Math.max(0,y),w,h);
 }
 private void move(Point p){
  if(overlay.isDisposed())return;
  Rectangle old=mouse.x<0?null:loupe(mouse);
  mouse=p;Rectangle now=loupe(p);
  if(old!=null)overlay.redraw(old.x-2,old.y-2,old.width+4,old.height+4,false);
  overlay.redraw(now.x-2,now.y-2,now.width+4,now.height+4,false);
 }

 private void paint(Event e){
  GC gc=e.gc;
  gc.drawImage(shot,e.x,e.y,e.width,e.height,e.x,e.y,e.width,e.height);
  if(mouse.x<0)return;
  Rectangle box=loupe(mouse);
  int half=CELLS/2;
  gc.setInterpolation(SWT.NONE);
  gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
  gc.fillRectangle(box);
  int sx=Math.max(0,Math.min(screen.width-CELLS,mouse.x-half)),sy=Math.max(0,Math.min(screen.height-CELLS,mouse.y-half));
  gc.drawImage(shot,sx,sy,CELLS,CELLS,box.x+1,box.y+1,LOUPE,LOUPE);
  int cx=box.x+1+(mouse.x-sx)*ZOOM,cy=box.y+1+(mouse.y-sy)*ZOOM;
  gc.setLineWidth(2);gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
  gc.drawRectangle(cx-1,cy-1,ZOOM+1,ZOOM+1);
  gc.setLineWidth(1);gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
  gc.drawRectangle(cx-2,cy-2,ZOOM+3,ZOOM+3);
  gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
  gc.drawRectangle(box.x,box.y,box.width-1,LOUPE+1);
  RGB rgb=at(mouse.x,mouse.y);
  Color swatch=new Color(display,rgb);
  try{
   int ty=box.y+LOUPE+4;
   gc.setBackground(swatch);gc.fillRectangle(box.x+6,ty+4,18,18);
   gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));gc.drawRectangle(box.x+6,ty+4,18,18);
   gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
   gc.drawText("#"+CustomColors.hex(rgb),box.x+32,ty+5,true);
  }finally{swatch.dispose();}
 }
}
