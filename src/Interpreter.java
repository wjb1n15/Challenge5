import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class Interpreter {
	
	protected ArrayList<Variable> varList;
	protected ArrayList<ArrayList<Variable>> varRecord;
	protected String[] code;
	protected Stack<Integer> loopStack;
	protected int pc;
	protected boolean running;
	protected int lastErr;
	
	public Interpreter()
	{
		varList = new ArrayList<Variable>();
		varRecord = new ArrayList<ArrayList<Variable>>();
		code = new String[0];
		loopStack = new Stack<Integer>();
		pc = 0;
		running = false;
		lastErr = -1;
	}
	
	public void setCode(String code)
	{
		this.code = code.split(";");
	}
	
	public ArrayList<Variable> getVariables()
	{
		return varList;
	}
	
	public ArrayList<ArrayList<Variable>> getVarRecord()
	{
		return varRecord;
	}
	
	public void cancel()
	{
		varList.clear();
		varRecord.clear();
		loopStack.clear();
		pc = 0;
		running = false;
		lastErr = -1;
	}
	
	public void setRunning(boolean running)
	{
		this.running = running;
	}
	
	public void run()
	{
		cancel();
		running = true;
		while(running)
			step();
	}
	
	protected ArrayList<String> getLine(int lineNum)
	{
		ArrayList<String> line = new ArrayList<String>(Arrays.asList(code[lineNum].split("\\s")));
		boolean comment = false;
		for(int i = 0; i < line.size(); i++) {
			if(line.get(i).equals("//")) {
				comment = !comment;
				line.remove(i);
				i--;
			} else if(comment || line.get(i).equals("")) {
				line.remove(i);
				i--;
			}
		}
		return line;
	}
	
	public void step()
	{
		if(pc >= code.length) {
			running = false;
			return;
		}
		
		getVar("pc").setValue(pc);
		
		ArrayList<String> line = getLine(pc);
		
		if(line.size() == 2) {
			if(line.get(0).equals("clear")) {
				getVar(line.get(1)).clear();
			} else if(line.get(0).equals("incr")) {
				getVar(line.get(1)).incr();
			} else if(line.get(0).equals("decr")) {
				getVar(line.get(1)).decr();
			} else {
				error();
				return;
			}
		} else if(line.size() == 5) {
			if(line.get(0).equals("while") && line.get(2).equals("not") && line.get(3).equals("0") && line.get(4).equals("do")) {
				if(getVar(line.get(1)).getValue() == 0) {
					int count = 1;
					
					while(count > 0 && pc < code.length - 1) {
						pc++;
						if(code[pc].contains("while"))
							count++;
						else if(getLine(pc).get(0).equals("end"))
							count--;
					}
					getVar("pc").setValue(pc);
					
					if(pc == code.length - 1) {
						running = false;
						return;
					}
				} else {
					loopStack.push(pc);
				}
			} else {
				error();
				return;
			}
		} else if(line.size() == 1 && line.get(0).equals("end")) {
			pc = loopStack.pop() - 1;
			getVar("pc").setValue(pc);
		} else if(line.size() == 1 && line.get(0).equals("abandon")) {
			loopStack.pop();
		} else if(line.size() == 0) {
		} else if(line.size() == 3 && line.get(0).equals("set")) {
			if(line.get(2).matches("[0-9]+"))
				getVar(line.get(1)).setValue(Integer.parseInt(line.get(2)));
			else
				getVar(line.get(1)).setValue(getVar(line.get(2)).getValue());;
		} else {
			error();
			return;
		}
		
		
		
		ArrayList<Variable> clone = new ArrayList<Variable>();
		
		for(Variable var : varList) {
			clone.add(new Variable(var));
		}
		
		varRecord.add(clone);
		
		pc = getVar("pc").getValue() + 1;
	}
	
	public void error()
	{
		lastErr = pc;
		running = false;
	}
	
	protected int posOf(String name)
	{
		for(int i = 0; i < varList.size(); i++) {
			if(varList.get(i).name.equals(name))
				return i;
		}
		
		return -1;
	}
	
	protected Variable getVar(String name)
	{
		int pos = posOf(name);
		if(pos == -1) {
			pos = varList.size();
			varList.add(new Variable(name));
		}
		return varList.get(pos);
	}
	
	public int getLastErr()
	{
		return lastErr;
	}
	
	public int getPC()
	{
		return pc;
	}

}
