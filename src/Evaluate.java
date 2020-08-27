
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.io.*;

public class Evaluate 
{
	int index;
	String expression;
	BigDecimal answer;
	final static String[] precedence = {"ln^sqrt","/","*","-","+","()"};
	public static void main(String[] args) 
	{
		System.out.println("Scientific calculator which can evaluate any arithmetic expression!");
		System.out.println("Program demonstrates implementation of the Shunting Yard Algorithm.");
		System.out.println("You can type 'END' at any time to terminate all processes.");
		System.out.println("---------------------------------------------------------------------------");
		System.out.println("Supported operators: ln()  ^  sqrt()  *  /  +  - ");
		System.out.println("Supported identifiers: e  pi ANS");
		System.out.println("Commands: 'END'  'format'");
		System.out.println("Note: \nln() function is calculates natural log."
				+ "\nTo input a negative number by itself (esp. -1), you must input (0-1) instead of -1."
				+ "\nInputting '--' is not allowed, rather '+' must be inputted to receive a valid answer.");
		System.out.println("---------------------------------------------------------------------------\n");

		Scanner input = new Scanner(System.in);
		String inp;
		Evaluate e = new Evaluate();
		while(true)
		{
			System.out.println("Enter an arithmetic expression to be evaluated:");
			inp = input.nextLine();
			if(inp.trim().toUpperCase().equals("END"))
			{
				System.out.println("---------------------------------------------------------------------------");
				System.out.println("Thank you for using this program!");
				System.out.println("-OVER-");
				System.exit(0);
			}
			if(inp.replace(" ","").toLowerCase().equals("format"))
			{
				System.out.println("ANSWER: " + format(e.getAnswer()) + "\n");
				continue;
			}
			e.setExpression(inp);
			e.evaluateExpression();
			if(String.valueOf(e.getAnswer()).length() > 50)
			{
				System.out.println("ANSWER: " + format(e.getAnswer()) + "\n");
			}
			else
				System.out.println("ANSWER: " + e.getAnswer() + "\n");

			System.out.println("---------------------------------------------------------------------------");
		}
	}
	
	public Evaluate(String i)
	{
		index = 0;
		expression = i;
		formatExpression();
		answer = null;
	}
	
	public Evaluate()
	{
		index = 0;
		expression = "";
		answer = null;
	}
	
	public void setExpression(String e) {expression = e; formatExpression(); index = 0;}
	public String getExpression() {return expression;}
	public BigDecimal getAnswer() {return answer;}
	/*
	 * -----------------------------------Shunting Yard Alg-----------------------------------
	 * https://brilliant.org/wiki/shunting-yard-algorithm/
	 * 
	 * 
	 * Pseudocode
	 * While there are tokens to be read:
2.        Read a token
3.        If it's a number add it to queue
4.        If it's an operator
5.               While there's an operator on the top of the stack with greater precedence:
6.                       Pop operators from the stack onto the output queue
7.               Push the current operator onto the stack
8.        If it's a left bracket push it onto the stack
9.        If it's a right bracket 
10.            While there's not a left bracket at the top of the stack:
11.                     Pop operators from the stack onto the output queue.
12.             Pop the left bracket from the stack and discard it
13. 	While there's operators on the stack, pop them to the queue
	 */
	
	public void evaluateExpression()
	{
		Stack<String> operators = new Stack<String>();
		Queue<String> output = new LinkedList<String>();

		boolean possible = true;
		String token = "null";
		while(true)
		{
			token = getToken();
			if(token.equals("")) //exit case, no more tokens
				break;
			
			
			String numeric = "";
			for(int i = 0; i < token.length(); i++)
				if(Character.isDigit(token.charAt(i)) || token.charAt(i) == '.')
					numeric += "T";
			if(numeric.length() == token.length())//number?
			{
				output.add(token);
			}
			else if(isOperator(token)) //operator?
			{
				while(!operators.isEmpty() && precIndexOf(operators.peek()) < precIndexOf(token)) //keep popping while operator of higher precedence is on stack
				{
					output.add(operators.pop());
				}
				operators.push(token);
			}
			else if(token.equals("(")) //left parentheses?
			{
				operators.push(token);
			}
			else if(!operators.empty() && token.equals(")")) //right parentheses?
			{
				while(!operators.empty() && !operators.peek().equals("(")) //until left P reached
				{
					output.add(operators.pop());
				}
				if(!operators.empty())
					operators.pop();
			}
			else
			{
				System.out.println("Error! Invalid input.");
				possible = false;
				break;
			}
		}
		
		while(possible && !operators.empty()) //transfer remaining operators to output stack
		{
			output.add(operators.pop());
		}
		
		Stack<BigDecimal> result = new Stack<BigDecimal>();
		
		while(possible && !output.isEmpty())
		{
			String s = output.poll();
			
			switch(s)
			{
			case "+": 
				if(result.size() >= 2)
					result.push(result.pop().add(result.pop()));
				else
				{
					System.out.println("Error! Invalid input.");
					possible = false;
				}
				break;
			case "-": 
				if(result.size() >= 2)
				{
					BigDecimal temps = result.pop();
					result.push(result.pop().subtract(temps));
				}
				else
				{
					System.out.println("Error! Invalid input.");
					possible = false;
				}
				break;
			case "*":
				if(result.size() >= 2)
					result.push(result.pop().multiply(result.pop()));
				else
				{
					System.out.println("Error! Invalid input.");
					possible = false;
				}
				break;
			case "/":
				if(result.size() >= 2)
				{
					BigDecimal tempd = result.pop();
					if(tempd.equals(0))
					{
						System.out.println("Error! Division by 0.");
						possible = false;
					}

					result.push(result.pop().divide(tempd,20,RoundingMode.HALF_EVEN));
					
				}
				else
				{
					System.out.println("Error! Invalid input.");
					possible = false;
				}
				break;
			case "^":
				if(result.size() >= 2)
				{
					int tempe = result.pop().intValue();
					result.push(result.pop().pow(tempe));
				}
				else
				{
					System.out.println("Error! Invalid input.");
					possible = false;
				}
				break;
			case "sqrt":
				if(result.size() >= 1)
					result.push(bigSqrt(result.pop()));
				else
				{
					System.out.println("Error! Invalid input.");
					possible = false;
				}
				break;
			case "ln":
				if(result.size() >= 1)
					result.push(BigDecimal.valueOf(Math.log(result.pop().doubleValue())));
				else
				{
					System.out.println("Error! Invalid input.");
					possible = false;
				}
				break;
			}
			if(isOperator(s))
				continue;
			
			
			String numeric = "";
			for(int i = 0; i < s.length(); i++)
				if(Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')
					numeric += "T";
			if(numeric.length() == s.length())
				result.push(BigDecimal.valueOf(Double.parseDouble(s)));
			else
			{
				System.out.println("Error! Invalid input.");
				possible = false;
			}
		}
		
		
		
		if(possible)
		{
			answer = result.pop();
		}
		else
			answer = null;
		

	}

	private static String format(BigDecimal x)
	{
		NumberFormat formatter = new DecimalFormat("0.0E0");
		formatter.setRoundingMode(RoundingMode.HALF_UP);
		formatter.setMinimumFractionDigits((x.scale() > 0) ? x.precision() : x.scale());
		return formatter.format(x);
	}

	private void formatExpression()
	{
		expression = expression.toLowerCase();
		expression = expression.replace(" ", "");
	}
	
	private String getToken()
	{
		String temp = ""; //will return no character string if no more tokens
		
		if(index < expression.length() && !(Character.isDigit(expression.charAt(index)) || Character.isLetter(expression.charAt(index))))
		{
			temp += expression.charAt(index);
			index++;
			return temp;
		}
		
		while(index < expression.length() && (Character.isDigit(expression.charAt(index)) || Character.isLetter(expression.charAt(index)) || expression.charAt(index) == '.'))
		{
			temp += expression.charAt(index);
			index++;
		}
		
		if(temp.equals("e"))
			temp = String.valueOf(Math.E);
		if(temp.equals("pi"))
			temp = String.valueOf(Math.PI);
		if(temp.equals("ans"))
			temp = String.valueOf(getAnswer());
		return temp;
	}
	
	private int precIndexOf(String s)
	{
		for(int i = 0; i < precedence.length; i++)
			if(precedence[i].contains(s))
				return i;
		return -1;
	}
	
	private boolean isOperator(String s)
	{
		if((s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/") || s.equals("^") || s.equals("sqrt") || s.equals("ln")))
			return true;
		else
			return false;
	}

	private static final BigDecimal SQRT_DIG = new BigDecimal(150);
	private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(SQRT_DIG.intValue());
	private static BigDecimal sqrtNewtonRaphson  (BigDecimal c, BigDecimal xn, BigDecimal precision){
		BigDecimal fx = xn.pow(2).add(c.negate());
		BigDecimal fpx = xn.multiply(new BigDecimal(2));
		BigDecimal xn1 = fx.divide(fpx,2*SQRT_DIG.intValue(), RoundingMode.HALF_DOWN);
		xn1 = xn.add(xn1.negate());
		BigDecimal currentSquare = xn1.pow(2);
		BigDecimal currentPrecision = currentSquare.subtract(c);
		currentPrecision = currentPrecision.abs();
		if (currentPrecision.compareTo(precision) <= -1){
			return xn1;
		}
		return sqrtNewtonRaphson(c, xn1, precision);
	}

	/**
	 * Uses Newton Raphson to compute the square root of a BigDecimal.
	 *
	 * @author Luciano Culacciatti
	 * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
	 */
	public static BigDecimal bigSqrt(BigDecimal c){
		return sqrtNewtonRaphson(c,new BigDecimal(1),new BigDecimal(1).divide(SQRT_PRE));
	}
}
