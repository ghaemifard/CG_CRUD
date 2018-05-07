package edu.islab.acceleo.update.common;
 

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
 
import update.*;

public class UpdateHelper {
	public boolean isNameBig(Field f){
		if(f.getName() == null)
			return false;
		return f.getName().length() > 10;
	}
	 
	 
	
	public boolean isThereRelations(TableSelection si) {
		return si.getRels().size() != 0;
	}
	
	public String tableOField(Field field){ 
		Field fi = (Field) field;
		 if(fi.getTable_ref() != null){
			return fi.getTable_ref().getName() + " AS " + fi.getTable_ref().getAlias();
		}
		else if(fi.getTableName() != null && fi.getTableName().length() > 0){
			return fi.getTableName();
		} 
	return "null";
}

public String fieldName(Field field){
	Field fi = (Field) field;
	String alias = "null";
	  if(fi.getTable_ref() != null){
		 alias = fi.getTable_ref().getAlias();
	}
	else if(fi.getTableName() != null && fi.getTableName().length() > 0){
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

public String tableOFActField(RealField field){ 
	RealField fi = (RealField) field;
	if(fi.getTable() != null){
		return fi.getTable().getName() + " AS " + fi.getTable().getAlias();
	} 
return "null";
}

public String actFieldName(RealField field){
	RealField fi = (RealField) field;
String alias = "null";
if(fi.getTable() != null){
	 alias =   fi.getTable().getAlias();
} 
return alias + "." + ((RealField)field).getName(); 
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
	for ( AbstractConditionElement ace : ci.getParts()) {
		if (ace instanceof RootJunction)
			return true;
	}
	return false;
}

public boolean isEqualDisType(JuncType dis, String a) {
	return ((dis == JuncType.AND && a.toLowerCase().equals("and"))
			|| (dis == JuncType.OR && a.toLowerCase().equals("or")));

}

public boolean isCallQuery(AbstractNode n) {
	return n instanceof CallSurfaceQuery;
}

public String generateNodep(Node an) {
	if (an instanceof Field) {
		Field f = (Field) an;
		return f.getName();

	} else if (an instanceof Literal) {
		Literal f = (Literal) an;
		return "\"" + f.getValue() + "\"";

	} else if (an instanceof NullLiteral) {
		NullLiteral f = (NullLiteral) an;
		return "null";

	} else if (an instanceof Input) {
		Input f = (Input) an;
		if (f.getInputRef()  != null) {
			return ":" + f.getInputRef().getName();
		}

	} else if (an instanceof CallSurfaceQuery) {
		CallSurfaceQuery f = (CallSurfaceQuery) an;

		String str = callTheQuerySurface(f.getSurfaceName());
		
		System.out.println("Surface generated");
		List<String> args = new ArrayList<String>();
		for (Node aa : f.getParams())
			args.add(generateNodep(aa ));
		
		
		

		List<String> ls = edu.islab.acceleo.query.common.ServiceTest.map.get(f.getSurfaceName());
		if (ls != null) {
			if (ls.size() == args.size()) {
				for (int i = 0; i < ls.size(); i++) {
					str = str.replace(ls.get(i), args.get(i));
				}
			}
		} else {
			System.out.println("LS is Null; in Insert");
		}
		return str;

	} else if (an instanceof Function) {
		Function f = (Function) an;
		if (!(f instanceof CallSurfaceQuery)) {
			String str = "";

			for (Node aa : f.getParams())
				str += generateNodep(aa ) + ",";

			return f.getName() + "( " + str.substring(0, str.length() - 1) + " ) ";
		}
	}
	return "";
}

private String callTheQuerySurface(String name){
	String path = Generate.arg0.substring(0, Generate.arg0.lastIndexOf("\\") + 1) + name + ".mext";
	String path2 = Generate.arg1 + "/" + name + ".sql";
	System.out.println("Path1: " + path);
	System.out.println("Path1: " + Generate.arg0);
	System.out.println("Path2: " + path2);
	String arr[] = {path,Generate.arg1};
	edu.islab.acceleo.query.common.Generate.main(arr);
	
	  try{
		  String str = new String(Files.readAllBytes(Paths.get(path2)));
		  return " ( "+str.trim()+" ) ";
	  }catch(Exception e){
		  
	  }
	  return "Er in calQuDelete";
}

public String prepareInput(InputInstalment input,String name) {
//	if(onCallQuery) return "";
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
//			String str2 = str;
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
			}
			else if (inp instanceof InputFloatDef) {
				sb.append("stm.setFloat(" + num + ", " + inp.getName() + "); \n\t");
			}
		}

		System.out.println("end 1");

	} catch (Exception e) {
		// TODO: handle exception
		e.printStackTrace();
	}
	return sb.toString().trim();

}

public String convertFileToJdbc(InputInstalment inin,String name) {
//	if(onCallQuery) return"";
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
	return res.trim();

}
}
