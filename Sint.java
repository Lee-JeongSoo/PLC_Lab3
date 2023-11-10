// Sint.java
// Interpreter for S
import java.util.Scanner;

public class Sint {
    static Scanner sc = new Scanner(System.in);
    static State state = new State();

    State Eval(Command c, State state) { 
        if (c instanceof Decl) {
            Decls decls = new Decls();
            decls.add((Decl) c);
            return allocate(decls, state);
        }
        if (c instanceof Stmt)
            return Eval((Stmt) c, state); 
		
	    throw new IllegalArgumentException("no command");
    }
  
    State Eval(Stmt s, State state) {
        if (s instanceof Empty) 
	        return Eval((Empty)s, state);
        if (s instanceof Assignment)  
	        return Eval((Assignment)s, state);
        if (s instanceof If)  
	        return Eval((If)s, state);
        if (s instanceof While)  
	        return Eval((While)s, state);
        if (s instanceof Stmts)  
	        return Eval((Stmts)s, state);
	    if (s instanceof Let)  
	        return Eval((Let)s, state);
	    if (s instanceof Read)  
	        return Eval((Read)s, state);
	    if (s instanceof Print)  
	        return Eval((Print)s, state);
        throw new IllegalArgumentException("no statement");
    }

    State Eval(Empty s, State state) {
        return state;
    }
  
    State Eval(Assignment a, State state) { 
        Value v = V(a.expr, state);
	    return state.set(a.id, v);
    }

    State Eval(Read r, State state) {
        if (r.id.type == Type.INT) {
	        int i = sc.nextInt();
	        state.set(r.id, new Value(i));
	    } 

	    if (r.id.type == Type.BOOL) {
	        boolean b = sc.nextBoolean();	
            state.set(r.id, new Value(b));
	    }
	    
	    if (r.id.type == Type.STRING) {
	        String s = (String) sc.next();
	        state.set(r.id, new Value(s));
	    }
		
	    return state;
    }

    State Eval(Print p, State state) {
	    System.out.println(V(p.expr, state));
        return state; 
    }
  
    State Eval(Stmts ss, State state) {
        for (Stmt s : ss.stmts) {
            state = Eval(s, state);
        }
        return state;
    }
  
    State Eval(If c, State state) {
        if (V(c.expr, state).boolValue( ))
            return Eval(c.stmt1, state);
        else
            return Eval(c.stmt2, state);
    }
 
    State Eval(While l, State state) {
        if (V(l.expr, state).boolValue( ))
            return Eval(l, Eval(l.stmt, state));
        else 
	        return state;
    }

    State Eval(Let l, State state) {
        State s = allocate(l.decls, state);
        s = Eval(l.stmts,s);
	    return free(l.decls, s);
    }
    
    State allocate (Decls ds, State state) {
        // add entries for declared variables (ds) on the state
        /* student exercise */
		if (ds != null) {
			for (Decl decl : ds) 
	            if (decl.expr == null) {
                    // TODO: [push a new variable with id and default Value] 57:41 참조 / 완료
                    // expr이 null이므로 각 타입에 맞는 default값을 이용해 초기화
                    state.push(decl.id, new Value(decl.type));
	            }
		        else {
                    // TODO: [push a new variable with id and value of V(expr)] 완료
                    //변수 선언과 동시에 지정된 초기화 값이 존재하므로 이를 이용해 스택에 푸시
                    state.push(decl.id, V(decl.expr, state));
		        }
			return state;
		}
    	
        return null;
    }

    State free (Decls ds, State state) {
    	// free the entries for declared variables (ds) from the state
    	/* student exercise */
    	if (ds != null) {
    		for (Decl decl : ds) {
    			// TODO: [pop a variable located at the top of stack]
                //단순 제거
                state.pop();
    		}
    		return state;
    	}
        return null;
    }

    Value binaryOperation(Operator op, Value v1, Value v2) { // Lab03
        check(!v1.undef && !v2.undef,"reference to undef value");
	    switch (op.val) {
	    case "+":
            return new Value(v1.intValue() + v2.intValue());
        case "-": 
            return new Value(v1.intValue() - v2.intValue());
        case "*": 
            return new Value(v1.intValue() * v2.intValue());
        case "/": 
            return new Value(v1.intValue() / v2.intValue());
            
        // TODO: [Add relational & logical operations] 완료
        /* student exercise */
        // Tip: Use String's compareTo() function for <, <=, >, => operations
            //비교하려는 값이 어떤 형식인지 확인이 필요. 따라서 if-else 문을 이용해 각 상황을 구분
            case "==":
                if (v1.type == Type.STRING)
                    return new Value(v1.value == v2.value);
                else if (v1.type == Type.BOOL)
                    return new Value(v1.boolValue() == v2.boolValue());
                else
                    return new Value(v1.intValue() == v2.intValue());
            case "!=":
                if (v1.type == Type.STRING)
                    return new Value(v1.value != v2.value);
                else if (v1.type == Type.BOOL)
                    return new Value(v1.boolValue() != v2.boolValue());
                else
                    return new Value(v1.intValue() != v2.intValue());
            case "<":
                //비교하는 값이 문자열인 경우
                if (v1.type == Type.STRING) {
                    //각 변수를 문자열로 변환하여 새로운 변수에 담는다.
                    String v1_str = (String)v1.value;
                    String v2_str = (String)v2.value;
                    // compareTo()메서드의 결과값을 담을 int값을 선언한다.
                    int res = v1_str.compareTo(v2_str);
                    //반환할 bool 값을 담을 변수를 선언한다.
                    boolean res_value;
                    if (res < 0) res_value = true;
                    else res_value = false;
                    return new Value(res_value);
                }
                // 비교하는 값이 문자열이 아닌경우, 이 경우 bool값은 대소 비교가 불가능하므로 int 값으로 취급한다.
                else
                    return new Value(v1.intValue() < v2.intValue());
            case "<=":
                if (v1.type == Type.STRING) {
                    String v1_str = (String)v1.value;
                    String v2_str = (String)v2.value;
                    int res = v1_str.compareTo(v2_str);
                    boolean res_value;
                    if (res <= 0) res_value = true;
                    else res_value = false;
                    return new Value(res_value);
                }
                else
                    return new Value(v1.intValue() <= v2.intValue());
            case ">":
                if (v1.type == Type.STRING) {
                    String v1_str = (String)v1.value;
                    String v2_str = (String)v2.value;
                    int res = v1_str.compareTo(v2_str);
                    boolean res_value;
                    if (res > 0) res_value = true;
                    else res_value = false;
                    return new Value(res_value);
                }
                else
                    return new Value(v1.intValue() > v2.intValue());
            case ">=":
                if (v1.type == Type.STRING) {
                    String v1_str = (String)v1.value;
                    String v2_str = (String)v2.value;
                    int res = v1_str.compareTo(v2_str);
                    boolean res_value;
                    if (res >= 0) res_value = true;
                    else res_value = false;
                    return new Value(res_value);
                }
                else
                    return new Value(v1.intValue() >= v2.intValue());

            //  AND, OR 연산은 Bool 타입에 대한 연산이므로 바로 Value()를 이용해 계산값을 반환한다.
            case "&":
                return new Value(v1.boolValue() && v2.boolValue());
            case "|":
                return new Value(v1.boolValue() || v2.boolValue());

            default:
                throw new IllegalArgumentException("no operation");
        }
    }

    Value unaryOperation(Operator op, Value v) {
        check( !v.undef, "reference to undef value");
	    switch (op.val) {
        case "!": 
            return new Value(!v.boolValue( ));
        case "-": 
            return new Value(-v.intValue( ));
        default:
            throw new IllegalArgumentException("no operation: " + op.val); 
        }
    } 

    static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
    }

    Value V(Expr e, State state) {
        if (e instanceof Value) 
            return (Value) e;

        if (e instanceof Identifier) {
	        Identifier v = (Identifier) e;
            return (Value)(state.get(v));
	    }

        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Value v1 = V(b.expr1, state);
            Value v2 = V(b.expr2, state);
            return binaryOperation (b.op, v1, v2); 
        }

        if (e instanceof Unary) {
            Unary u = (Unary) e;
            Value v = V(u.expr, state);
            return unaryOperation(u.op, v); 
        }
        throw new IllegalArgumentException("no operation");
    }

    public static void main(String args[]) {
	    if (args.length == 0) {
	        Sint sint = new Sint(); 
			Lexer.interactive = true;
            System.out.println("Language S Interpreter 2.0");
            System.out.print(">> ");
	        Parser parser  = new Parser(new Lexer());

	        do { // Program = Command*
	            if (parser.token == Token.EOF)
		            parser.token = parser.lexer.getToken();
	       
	            Command command=null;
                try {
	                command = parser.command();
	                if (command != null) command.display(0);    // display AST
	                else
						 throw new Exception();
                } catch (Exception e) {
                    System.out.println(e);
		            System.out.print(">> ");
                    continue;
                }

	            if (command.type != Type.ERROR) {
                    System.out.println("\nInterpreting..." );
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                         System.err.println(e);  
                    }
                }
		    System.out.print(">> ");
	        } while (true);
	    }
        else {
	        System.out.println("Begin parsing... " + args[0]);
	        Command command = null;
	        Parser parser  = new Parser(new Lexer(args[0]));
	        Sint sint = new Sint();

	        do {	// Program = Command*
	            if (parser.token == Token.EOF)
                    break;
	         
                try {
	                command = parser.command();
	                if (command != null) command.display(0);    // display AST
	                else
	                	throw new Exception();
                } catch (Exception e) {
                    System.out.println(e);
                    continue;
                }

	            if (command.type!=Type.ERROR) {
                    System.out.println("\nInterpreting..." + args[0]);
//                    for(Pair pair : state){
//                        System.out.println("스택 확인" + pair.val.value);
//                    }
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                        System.err.println(e);  
                    }
                }
	        } while (command != null);
        }        
    }
}