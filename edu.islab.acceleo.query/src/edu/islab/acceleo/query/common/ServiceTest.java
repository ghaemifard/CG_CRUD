package edu.islab.acceleo.query.common;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl.SettingMany;

import query.AbstractNode;
import query.CallQuerySurface;
import query.ConditionInstalment;
import query.Field;
import query.Function;
import query.Input;
import query.InputBitsDef;
import query.InputDateDef;
import query.InputDef;
import query.InputFloatDef;
import query.InputInstalment;
import query.InputIntDef;
import query.InputStringDef;
import query.JuncType;
import query.Literal;
import query.MyTable;
import query.Node;
import query.NullLiteral;
import query.OutputInstalment;
import query.QuerySurface;
import query.RelationSelect;
import query.RootJunction;

public class ServiceTest {
	public static Map<String, List<String>> map = new HashMap<String, List<String>>();
	public static List<String> listOfName = new ArrayList<String>();
	private static boolean onCallQuery = false;
	public boolean isNameBig(Field f) {
		if (f.getName() == null)
			return false;
		return f.getName().length() > 10;
	}

	public boolean isThereFirstTable(OutputInstalment si) {
		boolean bl = false;
		for (AbstractNode an : si.getNodes()) {
			if (an instanceof MyTable || an instanceof Field) {
				bl = true;
				break;
			}
		}
		return bl;
	}

	public String getFirstTable(OutputInstalment si) {
		for (AbstractNode an : si.getNodes()) {
			if (an instanceof MyTable) {
				return ((MyTable) an).getName() + " as " + ((MyTable) an).getAlias();
			} else if (an instanceof Field) {
				Field f = (Field) an;
				if (f.getTable() == null) {
					if (f.getTableName() != null) {
						return " " + f.getTableName() + " ";
					} else {
						return "<table>";
					}
				} else {
					return ((MyTable) f.getTable()).getName() + " as " + ((MyTable) f.getTable()).getAlias();
				}
			}
		}
		return "";
	}

	public boolean isThereRelations(OutputInstalment si) {
		return si.getRels().size() != 0;
	}

	public String tableOField(Field field) {
		Field fi = (Field) field;
		if (fi.getTable() != null) {
			return fi.getTable().getName() + " AS " + fi.getTable().getAlias();
		} else if (fi.getTable_ref() != null) {
			return fi.getTable_ref().getName() + " AS " + fi.getTable_ref().getAlias();
		} else if (fi.getTableName() != null && fi.getTableName().length() > 0) {
			return fi.getTableName();
		}
		return "null";
	}

	public String fieldName(Field field) {
		Field fi = (Field) field;
		String alias = "null";
		if (fi.getTable() != null) {
			alias = fi.getTable().getAlias();
		} else if (fi.getTable_ref() != null) {
			alias = fi.getTable_ref().getAlias();
		} else if (fi.getTableName() != null && fi.getTableName().length() > 0) {
			alias = fi.getTableName();
		}
		String post = "";
		if(((Field)field).getColumn_name() != null && ((Field)field).getColumn_name().length() > 0){
			post = ((Field)field).getColumn_name();
		}else{
			post = ((Field)field).getName();
		}
		return alias + "." + post; 
	}

	public String getSelectRelationType(RelationSelect rs) {
		switch (rs.getOpType()) {
		case EQUAL:

			return "=";
		case GEREATER_EQUAL:

			return ">=";
		case GREATER:

			return ">";
		case LOWER:

			return "<";
		case LOWER_EQUAL:
			return "<=";
		}
		return "=";
	}

	public boolean isThereRoot(ConditionInstalment ci) {
		if (ci == null)
			return false;
		for (query.AbstractConditionElement ace : ci.getParts()) {
			if (ace instanceof RootJunction)
				return true;
		}
		return false;
	}

	public boolean isEqualDisType(JuncType dis, String a) {
		return ((dis == JuncType.AND && a.toLowerCase().equals("and"))
				|| (dis == JuncType.OR && a.toLowerCase().equals("or")));

	}

	private static int Count = 0;

	public boolean isCallQuery(AbstractNode n) {
		return n instanceof CallQuerySurface;
	}

	public String callQuryNow(String name,  Integer n) {
		onCallQuery = true;
		String path = Generate.arg0.substring(0, Generate.arg0.lastIndexOf("\\") + 1) + name + ".mext";
		String dest = Generate.arg1 + "/Boom" + (Count++);
		File f = new File(path);
		if (f.exists()) {
			List<String> args = new ArrayList<String>();
			File store = new File(dest);
			if (!store.exists()) {
				store.mkdir();
			}
			try {
				String prev_arg0 = Generate.arg0;
				String prev_arg1 = Generate.arg1;
				Generate g = new Generate(URI.createFileURI(path), store, args);
				g.doGenerate(new BasicMonitor());
				String associatedClass = listOfName.remove(listOfName.size()-1);
				;
				byte[] bts = Files.readAllBytes(Paths.get(dest + "/"+associatedClass+".sql"));
				String str = " ( " + new String(bts) + " ) ";
				// store.delete();
				onCallQuery = false;
				Generate.arg0 = prev_arg0;
				Generate.arg1 = prev_arg1;
				resetCounter(null);
				return str;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			onCallQuery = false;
			resetCounter(null);
			return "NoPath: " + path;
		}
		resetCounter(null);
		onCallQuery = false;
		return "Hi: " + path + " : " + Generate.arg1;
	}

	private static int counter = 0;

	public int getCounter(AbstractNode qs) {
		counter = counter + 1;
		System.out.println(counter);
		return counter;
	}

	public String resetCounter(AbstractNode qs) {
		counter = 0;
		return "";
	}

	public String inputToStr(InputDef in) {
		return ":" + in.getName();
	}

	public String generateNodep(AbstractNode an) {
		if (an instanceof Field) {
			Field f = (Field) an;
			return fieldName(f);

		} else if (an instanceof MyTable) {
			MyTable f = (MyTable) an;
			if (f.getFields().size() > 0) {
				String str = f.getAlias() + "." + f.getFields().get(0).getName();
				for (int i = 1; i < f.getFields().size(); i++) {
					str += "," + f.getAlias() + "." + f.getFields().get(i).getName();
				}
				return str;

			}
		} else if (an instanceof Literal) {
			Literal f = (Literal) an;
			return "\"" + f.getValue() + "\"";

		} else if (an instanceof NullLiteral) {
			NullLiteral f = (NullLiteral) an;
			return "null";

		} else if (an instanceof Input) {
			Input f = (Input) an;
			if (f.getInputRef() != null) {
				return ":" + f.getInputRef().getName();
			}

		} else if (an instanceof CallQuerySurface) {
			CallQuerySurface f = (CallQuerySurface) an;
 
			String str = callQuryNow(f.getSurfaceName(),  0);
			System.out.println("Surface generated");
			List<String> args = new ArrayList<String>();
			for (Node aa : f.getParams())
				args.add(generateNodep(aa ));

			List<String> ls = map.get(f.getSurfaceName());
			if (ls != null) {
				if (ls.size() == args.size()) {
					for (int i = 0; i < ls.size(); i++) {
						str = str.replace(ls.get(i), args.get(i));
					}
				}
			} else {
				System.out.println("LS is Null;");
			}
			return str;

		} else if (an instanceof Function) {
			Function f = (Function) an;
			if (!(f instanceof CallQuerySurface)) {
				String str = "";

				for (Node aa : f.getParams())
					str += generateNodep(aa ) + ",";

				return f.getName() + "( " + str.substring(0, str.length() - 1) + " ) ";
			}
		}
		return "";
	}

	public void insertToMap(QuerySurface qs) {
		System.out.println("Insert called: " + qs.getName());
		listOfName.add(qs.getAssociatedClass());
		List<String> ls = map.get(qs.getName());
		if (ls != null)
			return;
		ls = new ArrayList<String>();
		map.put(qs.getName(), ls);
		if (qs.getInputInstalment() != null) {

			for (InputDef i : qs.getInputInstalment().getInputs()) {
				ls.add(":" + i.getName());
			}
		}
	}


	public String prepareInput(InputInstalment input,String name) {
		if(onCallQuery) return "";
		StringBuilder sb = new StringBuilder();
		try {

			Map<Integer, Integer> map = new HashMap<Integer, Integer>();

			System.out.println("start 0");
			byte[] bts = Files.readAllBytes(Paths.get(Generate.arg1 + "/"+name+".sql"));
			String str = new String(bts);
			Set<Integer> set = new HashSet<Integer>();
			str = str.replace("\n", "");
			System.out.println("start 1:" + str.length());
			for (InputDef i : input.getInputs()) {
				String match = ":" + i.getName();
//				String str2 = str;
				StringBuilder str2 = new StringBuilder(str);
				
				while (true) {
					int index = str2.indexOf(match);
					if (index < 0) {
						break;
					}
					set.add(index);
					map.put(index, input.getInputs().indexOf(i));
					str2 = str2.replace(index, index+1, " ");
				}
			}

			System.out.println("start 2:" + set.size());
			int num = 0;
			for (int i : set) {
				num++;
				InputDef inp = input.getInputs().get(map.get(i));
				if (inp instanceof InputDateDef) {
					sb.append("stm.setDate(" + num + ", " + inp.getName() + "); \n\t");
				} else if (inp instanceof InputBitsDef) {
					sb.append("stm.setInt(" + num + ", " + inp.getName() + "); \n\t");
				} else if (inp instanceof InputIntDef) {
					sb.append("stm.setInt(" + num + ", " + inp.getName() + "); \n\t");
				} else if (inp instanceof InputStringDef) {
					sb.append("stm.setString(" + num + ", " + inp.getName() + "); \n\t");
				} else if (inp instanceof InputFloatDef) {
					sb.append("stm.setFloat(" + num + ", " + inp.getName() + "); \n\t");
				}
			}

			System.out.println("end 1");

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return sb.toString();

	}

	public String convertFileToJdbc(InputInstalment inin,String name) {
		if(onCallQuery) return"";
		String res = "";
		try {
			byte[] bts = Files.readAllBytes(Paths.get(Generate.arg1 + "/"+name+".sql"));
			res = new String(bts); 
			res = res.replace("\n", " "); 
			res = res.replace("\t", " "); 
			res = res.replace("\"", "\\\""); 
			res = res.replace("  ", " ");
			if (inin == null)
				return res;
			for(InputDef i : inin.getInputs()){
				res = res.replaceAll(":"+i.getName(), " ? ");
			}
		} catch (Exception e) { 
			e.printStackTrace();
		}
		return res;

	}
	public String appendMethod(QuerySurface qs){
		if(onCallQuery) return"";
		try {
			byte[] bts = Files.readAllBytes(Paths.get(Generate.arg1 + "/"+qs.getName()+"_Func.java")); 
			byte[] total = Files.readAllBytes(Paths.get(Generate.arg1 + "/"+Character.toUpperCase(qs.getAssociatedClass().charAt(0)) + qs.getAssociatedClass().substring(1) +".java"));
			
			String totalStr = new String(total);
			String btsStr = new String(bts);
			boolean bl = true;
			
			StringReader strReader = new StringReader(btsStr);
			Scanner sc = new Scanner(btsStr);
			String mainline = "";
			while(sc.hasNextLine() ){
				String line = sc.nextLine();
				if(line.trim().startsWith("public static"))
					mainline = line;
			}
			if(totalStr.contains(mainline)){
				int index = totalStr.indexOf(mainline);
				int count = 0;
				int start = index;
				int end = 0;
				while(index < totalStr.length()){
					if(totalStr.charAt(index++) == '{'){
						count++;
						break;
					}
				}
				while(count !=0){ 
					if(totalStr.charAt(index) == '{'){
						count++; 
					}else if(totalStr.charAt(index) == '}'){
						count--; 
					}
					index++;
				}
				end = index;
				StringBuilder sb = new StringBuilder();
				
				for(int i=0;i<start;i++){
					sb.append(totalStr.charAt(i));
				}
				
				sb.append("\n"+btsStr + "\n");

				for(int i=end;i<totalStr.length();i++){
					sb.append(totalStr.charAt(i));
				}
				StringBuilder fin = new StringBuilder();
//				sc = new Scanner(sb.toString());
//				String prev = "a";
//				while(sc.hasNextLine()){
//					String line = sc.nextLine();
//					if(line.trim().length() != 0 ){
//						fin.append(line);
//					}else{
//						fin.append("\n");
//					}
//				}
//				String enda = fin.toString().replace("\n\n", "\n");
				Files.write(Paths.get(Generate.arg1 + "/"+Character.toUpperCase(qs.getAssociatedClass().charAt(0)) + qs.getAssociatedClass().substring(1) +".java") , fin.toString().getBytes() );
			
			}else{

				for(int i=total.length-1;i>=0;i--){
					if(total[i] == "}".getBytes()[0]){
						total[i] = 0x20;
						break;
					}
				}
				
				String fina = new String(total) + new String(bts) +"\n\n}";
				Files.write(Paths.get(Generate.arg1 + "/"+Character.toUpperCase(qs.getAssociatedClass().charAt(0)) + qs.getAssociatedClass().substring(1) +".java") , fina.getBytes() );
			}
			
		} catch (Exception e) {
			e.printStackTrace(); 
		}
		return "";
	}

}
