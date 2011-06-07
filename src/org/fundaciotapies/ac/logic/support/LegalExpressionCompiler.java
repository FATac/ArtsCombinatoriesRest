package org.fundaciotapies.ac.logic.support;

import java.util.Properties;

public class LegalExpressionCompiler {
	
	String bool = "(?i)true|false";
	String identifier = "[a-zA-Z_][a-zA-Z0-9_]*";
	String number = "[0-9][0-9]*";
	String[] operators = { "||", "&&", "=", "!=", "<", ">" }; // IMPORTANT!! Operators MUST BE ORDERED BY GROUPING PRIORITY	
	
	Properties data = new Properties();
	
	public String[] extractOperands(String exp) {
		String[] result = { null, null, null, null };
		
		exp = exp.trim();
		
		while (exp.startsWith("!")) {
			if (result[0]==null) result[0] = "";
			result[0] += "!";
			exp = exp.substring(1).trim();
		}
		
		if (exp.startsWith("(")) {
			int lastIdx = 0;
			int tmpIdx = 0;
			while ((tmpIdx=exp.indexOf(")",lastIdx+1)) > 0) {
				lastIdx = tmpIdx;
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
		
		if (tokens[1].matches(bool)) {
			resultL = Boolean.parseBoolean(tokens[1].toLowerCase());
		} else if (tokens[1].matches(identifier)) {
			String value = data.getProperty(tokens[1]);
			if (value==null) throw new LegalExpressionException("Variable not defined " + tokens[1]);
			if (value.matches(bool)) {
				resultL = Boolean.parseBoolean(value);
			} else if (value.matches(number)) {
				resultL = Integer.parseInt(value);
			} else { 
				throw new LegalExpressionException("Expression error");
			}
		} else if (tokens[1].matches(number)) {
			resultL = Integer.parseInt(tokens[1]);
		} else resultL = eval(tokens[1]);
		
		if (tokens[0]!=null) {
			if (resultL instanceof Boolean) { 
				if (tokens[0].length() % 2 != 0) resultL = !(Boolean)resultL;
			} 
			else throw new LegalExpressionException("Operator (!) only valid for boolean");
		}
		
		if (tokens[3] != null && tokens[2] !=null) {
			Object resultR = eval(tokens[3]);
			String operator = tokens[2]; 
			if (operator.equals(operators[2])) { // EQUALS
				return resultL.equals(resultR);
			} else if (operator.equals(operators[3])) { // NOT-EQUALS
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
