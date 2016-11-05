
public class Variable {
	
	protected String name;
	protected int value = 0;
	
	public Variable(String name)
	{
		this.name = name;
	}
	
	public Variable(Variable cloneFrom)
	{
		name = cloneFrom.getName();
		value = cloneFrom.getValue();
	}
	
	public void incr()
	{
		value++;
	}
	
	public void decr()
	{
		value--;
	}
	
	public void clear()
	{
		value = 0;
	}
	
	public void setValue(int value)
	{
		this.value = value;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getValue()
	{
		return value;
	}
	
}
