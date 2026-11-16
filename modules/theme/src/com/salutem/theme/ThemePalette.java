package com.salutem.theme;
import java.util.*;import java.io.*;
public final class ThemePalette {
 public final String id,label;public final int background,foreground,border,accent,muted;
 private ThemePalette(Properties p,String prefix){id=p.getProperty(prefix+".id");label=p.getProperty(prefix+".label");background=color(p,prefix,"background");foreground=color(p,prefix,"foreground");border=color(p,prefix,"border");accent=color(p,prefix,"accent");muted=color(p,prefix,"muted");}
 private static int color(Properties p,String prefix,String key){return Integer.parseInt(p.getProperty(prefix+"."+key),16);}
 private static final Map<String,ThemePalette> PALETTES=load();
 private static Map<String,ThemePalette> load(){Properties p=new Properties();try(InputStream stream=ThemePalette.class.getResourceAsStream("/palettes.properties")){if(stream==null)throw new IOException("palettes.properties missing");p.load(stream);}catch(IOException error){throw new IllegalStateException(error);}Map<String,ThemePalette> values=new LinkedHashMap<>();for(String prefix:p.getProperty("ids").split(",")){ThemePalette palette=new ThemePalette(p,prefix);values.put(palette.id,palette);}return Collections.unmodifiableMap(values);}
 public static ThemePalette find(String id){return PALETTES.get(id);}
 public static Collection<ThemePalette> all(){return PALETTES.values();}
}
