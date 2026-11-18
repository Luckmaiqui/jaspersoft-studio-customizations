package com.salutem.theme;
import java.util.*;
import java.util.function.Consumer;
import org.eclipse.swt.widgets.*;
/** Coalesces UI refresh bursts and removes overlapping parent/child work. */
final class RefreshQueue {
 private final Display display;private final Consumer<Control> refresh;
 private final Set<Control> pending=Collections.newSetFromMap(new IdentityHashMap<>());
 private boolean queued;
 RefreshQueue(Display display,Consumer<Control> refresh){this.display=display;this.refresh=refresh;display.disposeExec(pending::clear);}
 void request(Control control){
  if(control==null || control.isDisposed() || display.isDisposed())return;
  pending.add(control);if(queued)return;queued=true;
  display.timerExec(100,()->{
   queued=false;Control[] roots=pending.toArray(new Control[0]);pending.clear();
   Set<Control> batch=Collections.newSetFromMap(new IdentityHashMap<>());Collections.addAll(batch,roots);
   for(Control root:roots){if(root.isDisposed())continue;boolean covered=false;for(Control p=root.getParent();p!=null;p=p.getParent())if(batch.contains(p)){covered=true;break;}if(!covered)refresh.accept(root);}
  });
 }
}

