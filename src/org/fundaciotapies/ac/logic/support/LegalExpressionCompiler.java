package org.fundaciotapies.ac.logic.support;

import java.util.Properties;

public class LegalExpressionCompiler {
	private String bool = "(?i)true|false";
	private String identifier = "[a-zA-Z_][a-zA-Z0-9_]*";
	private String number = "[0-9][0-9]*";
	private String[] operators = { "||", "&&", "!=", "=", "<", ">" }; // IMPORTANT!! Operators MUST BE ORDERED BY GROUP DELIMITING PRIORITY
	private String string = "'.*'";
	 
	private Properties data = new Properties();
	
	public String[] extractOperands(String exp) throws LegalExpressionException {
		String[] result = { null, null, null, null };
		
		exp = exp.trim();
		
		while (exp.startsWith("!")) {
			if (result[0]==null) result[0] = "";
			result[0] += "!";
			exp = exp.substring(1).trim();
		}
		
		if (exp.startsWith("(")) {
			int lastIdx = 0;
			int par = 1;
			while((par>0) && (lastIdx < exp.length()-1)) {
				int tmpClose = exp.indexOf(")",lastIdx+1);
				int tmpOpen = exp.indexOf("(",lastIdx+1);
				if (tmpClose<tmpOpen && tmpClose!=-1 && tmpClose!=-1 || tmpClose!=-1 && tmpOpen==-1) {
					lastIdx = tmpClose;
					par--;
				} else if (tmpOpen<tmpClose && tmpOpen!=-1 && tmpOpen!=-1 || tmpOpen!=-1 && tmpClose==-1) {
					lastIdx = tmpOpen;
					par++;
				} else throw new LegalExpressionException("Syntax error");
			}
			
			result[1] = exp.substring(1, lastIdx);
			exp = exp.substring(lastIdx+1).trim();
			
			for (String o : operators) {
				if (exp.startsWith(o)) {
					result[2] = o;
					result[3] = exp.substring(o.length());
				}
			}
		} else {
			exp = (result[0]!=null?result[0]:"")+exp;
			result[0] = null;
			int closestIdx = 99999;
			String closestOp = null;
			for (String o : operators) {
				int idx = exp.indexOf(o);
				if (idx != -1) {
					closestIdx = idx;
					closestOp = o;
					break;
				}
			}
			
			if (closestOp != null) {
				result[1] = exp.substring(0, closestIdx).trim();
				result[2] = closestOp;
				result[3] = exp.substring(closestIdx + closestOp.length()).trim();
			} else {
				while (exp.startsWith("!")) {
					if (result[0]==null) result[0] = "";
					result[0] += "!";
					exp = exp.substring(1).trim();
				}
				result[1] = exp;
			}
		}
		
		return result;
	}
	
	public Object eval(String exp) throws LegalExpressionException {
		if (exp==null) return null;
		Object resultL = null;
		
		String[] tokens = extractOperands(exp);
		
		if (tokens[1].matches(identifier) && !tokens[1].matches(bool)) {
			String value = data.getProperty(tokens[1]);
			if (value!=null && !value.matches(bool) && !value.matches(number)) tokens[1] = "'"+value+"'";
			else tokens[1] = value;
		}
		
		if (tokens[1] != null) {
			if (tokens[1].matches(bool)) {
				resultL = Boolean.parseBoolean(tokens[1].toLowerCase());
			} else if (tokens[1].matches(number)) {
				resultL = Integer.parseInt(tokens[1]);
			} else if (tokens[1].matches(string)) {
				resultL = tokens[1].subSequence(1, tokens[1].length()-1);
			} else resultL = eval(tokens[1]);
		} else {
			resultL = new Object(); // Empty object
		}
		
		if (tokens[0] != null) {
			if (resultL instanceof Boolean) { 
				if (tokens[0].length() % 2 != 0) resultL = !(Boolean)resultL;
			} 
			else throw new LegalExpressionException("Operator (!) only valid for boolean");
		}
		
		if (tokens[3] != null && tokens[2] !=null) {
			Object resultR = eval(tokens[3]);
			String operator = tokens[2]; 
			if (operator.equals(operators[3])) { // EQUALS
				return resultL.equals(resultR);
			} else if (operator.equals(operators[2])) { // NOT-EQUALS
				return !resultL.equals(resultR);
			} else if (operator.equals(operators[4])) { // LESS THAN (int only)
				if (resultL instanceof Integer && resultR instanceof Integer) {
					return (Integer)resultL < (Integer)resultR;
				} else throw new LegalExpressionException("Types incompatibility with operator " + operators[4]);
			} else if (operator.equals(operators[5])) { // MORE THAN (int only)
				if (resultL instanceof Integer && resultR instanceof Integer) {
					return (Integer)resultL > (Integer)resultR;
				} else throw new LegalExpressionException("Types incompatibility with operator " + operators[5]);
			} else if (operator.equals(operators[1])) { // AND (boolean only)
				if (resultL instanceof Boolean && resultR instanceof Boolean) {
					return (Boolean)resultL && (Boolean)resultR;
				} else throw new LegalExpressionException("Types incompatibility with operator " + operators[0]);
			} else if (operator.equals(operators[0])) { // OR (boolean only)
				if (resultL instanceof Boolean && resultR instanceof Boolean) {
					return (Boolean)resultL || (Boolean)resultR;
				} else throw new LegalExpressionException("Types incompatibility with operator " + operators[1]);
			} else throw new LegalExpressionException("Operator unknown " + operator);
		} else {
			return resultL;
		}
	}

	public void setData(Properties data) {
		this.data = data;
	}

	public Properties getData() {
		return data;
	}
}
