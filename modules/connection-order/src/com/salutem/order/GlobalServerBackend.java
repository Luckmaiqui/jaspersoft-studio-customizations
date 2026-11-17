package com.salutem.order;

import java.util.*;
import java.util.function.BooleanSupplier;
import org.eclipse.core.runtime.*;
import com.jaspersoft.studio.server.AFinderUI;
import com.jaspersoft.studio.server.model.server.MServerProfile;

/** Uses Studio's authenticated client, not copied credentials or local report files. */
final class GlobalServerBackend implements GlobalSearchEngine.Backend {
 public List<GlobalSearchEngine.Hit> search(GlobalSearchEngine.Target target,String query,BooleanSupplier canceled)throws Exception{
  IProgressMonitor monitor=new NullProgressMonitor(){public boolean isCanceled(){return canceled.getAsBoolean() || super.isCanceled();}};
  if(!NativeServerSearch.connect(target.connection(),monitor))throw new OperationCanceledException();
  List<GlobalSearchEngine.Hit> hits=new ArrayList<>();
  AFinderUI finder=new AFinderUI((MServerProfile)target.connection()){
   @SuppressWarnings("rawtypes") public void showResults(List results){
    if(results==null)return;
    for(Object value:results){if(monitor.isCanceled())throw new OperationCanceledException();
     hits.add(new GlobalSearchEngine.Hit(target,property(value,"getLabel"),property(value,"getUri"),property(value,"getResourceType"),value));
    }
   }
  };
  finder.setText(query);
  Object client=NativeServerSearch.client(target.connection());
  client.getClass().getMethod("findResources",IProgressMonitor.class,AFinderUI.class).invoke(client,monitor,finder);
  return hits;
 }
 private static String property(Object value,String name){try{return Objects.toString(value.getClass().getMethod(name).invoke(value),"");}catch(ReflectiveOperationException error){return "";}}
}
