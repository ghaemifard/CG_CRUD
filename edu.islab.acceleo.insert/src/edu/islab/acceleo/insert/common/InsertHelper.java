package edu.islab.acceleo.insert.common;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.URI;
 
import insert.AbstractNode;
import insert.CallSurfaceQuery;
import insert.Field;
import insert.Function;
import insert.Input;
import insert.InputBitsDef;
import insert.InputDateDef;
import insert.InputDef;
import insert.InputInstalment;
import insert.InputIntDef;
import insert.InputStringDef;
import insert.Literal;
import insert.Node;
import insert.NullLiteral;  
 

public class InsertHelper {

	public String prepareInput(InputInstalment input,String name) {
//		if(onCallQuery) return "";
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
				}
//				else if (inp instanceof InputFloatDef) {
//					sb.append("stm.setFloat(" + num + ", " + inp.getName() + "); \n\t");
//				}
			}

			System.out.println("end 1");

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return sb.toString();

	}

	public String convertFileToJdbc(InputInstalment inin,String name) {
//		if(onCallQuery) return"";
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
	
	
	public boolean isCallQuery(AbstractNode n) {
		return (n instanceof CallSurfaceQuery);
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
			if (f.getInpDef() != null) {
				return ":" + f.getInpDef().getName();
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
		System.out.println("Path2: " + path2);
		String arr[] = {path,Generate.arg1};
		edu.islab.acceleo.query.common.Generate.main(arr);
		
		  try{
			  String str = new String(Files.readAllBytes(Paths.get(path2)));
			  return " ( "+str+" ) ";
		  }catch(Exception e){
			  
		  }
		  return "Er in calQuInsert";
	}

}
