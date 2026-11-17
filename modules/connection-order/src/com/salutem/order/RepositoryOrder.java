package com.salutem.order;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import com.jaspersoft.studio.repository.RepositoryView;
import com.jaspersoft.studio.model.ANode;

/** Sorts connection nodes in the repository viewer, without modifying the stored models. */
public final class RepositoryOrder extends ViewerComparator {
 private final NumericOrder numeric=new NumericOrder();
 private final ViewerComparator previous;
 public RepositoryOrder(ViewerComparator previous){this.previous=previous;}
 static int kind(Object node){
  for(Class<?> type=node==null?null:node.getClass();type!=null;type=type.getSuperclass()){
   if(type.getName().equals("com.jaspersoft.studio.data.MDataAdapter"))return 1;
   // Do not introduce a mandatory dependency on the optional server plug-in.
   if(type.getName().equals("com.jaspersoft.studio.server.model.server.MServerProfile"))return 2;
  }
  return 0;
 }
 private String label(Viewer viewer,Object node){
  if(viewer instanceof ContentViewer){IBaseLabelProvider provider=((ContentViewer)viewer).getLabelProvider();if(provider instanceof ILabelProvider)return ((ILabelProvider)provider).getText(node);}
  return node instanceof ANode?((ANode)node).getDisplayText():String.valueOf(node);
 }
 @Override public int compare(Viewer viewer,Object left,Object right){
  int category=kind(left);
  if(category!=0 && category==kind(right))return numeric.compare(label(viewer,left),label(viewer,right));
  return previous==null?0:previous.compare(viewer,left,right);
 }
 @Override public void sort(Viewer viewer,Object[] elements){
  if(previous!=null)previous.sort(viewer,elements);
  // Preserve other categories and their positions, even for mixed sibling lists.
  for(int category=1;category<=2;category++){
   List<Integer> positions=new ArrayList<>();List<Object> nodes=new ArrayList<>();
   for(int i=0;i<elements.length;i++)if(kind(elements[i])==category){positions.add(i);nodes.add(elements[i]);}
   nodes.sort((a,b)->numeric.compare(label(viewer,a),label(viewer,b)));
   for(int i=0;i<nodes.size();i++)elements[positions.get(i)]=nodes.get(i);
  }
 }
 @Override public boolean isSorterProperty(Object element,String property){return kind(element)!=0 || (previous!=null && previous.isSorterProperty(element,property));}
 public static void attach(TreeViewer viewer){
  if(viewer==null || viewer.getControl().isDisposed() || viewer.getComparator() instanceof RepositoryOrder)return;
  Object[] expanded=viewer.getExpandedElements();ISelection selection=viewer.getSelection();
  viewer.setComparator(new RepositoryOrder(viewer.getComparator()));
  viewer.setExpandedElements(expanded);viewer.setSelection(selection,false);
 }
 public static void attachOpenViews(){
  if(!PlatformUI.isWorkbenchRunning())return;
  for(IWorkbenchWindow window:PlatformUI.getWorkbench().getWorkbenchWindows())for(IWorkbenchPage page:window.getPages())for(IViewReference reference:page.getViewReferences()){
   IViewPart part=reference.getView(false);
   if(part instanceof RepositoryView)attach(((RepositoryView)part).getTreeViewer());
  }
 }
}
