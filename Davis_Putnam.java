package davis_putnam_algoritmus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

class Clause
{
	ArrayList<String> literals;
	Clause()
	{
		this.literals = new ArrayList<String>(); // Literálok String listája
	}
	void addLiteral(String literal) // Literál hozzáadása
	{
		literals.add(literal);
	}
	String printClause() // Kloz kiiratása
	{
		String clause = "[";
		boolean first = true;
		for(String l : literals)
		{
			if(first)
			{
				clause += l;
				first = false;
			}
			else
			{
				clause += " || "+l;
			}
		}
		return clause+"]";
	}
}

public class Davis_Putnam_algoritmus 
{	
	// A literálok értékeit itt tároljuk:
	static HashMap<String,Boolean> literalMap = new HashMap<String,Boolean>();
	public static void main(String args[]) throws IOException
	{
		// Fő lista, amelyben a kezdeti klózok vannak:
		ArrayList<Clause> Clauses = new ArrayList<Clause>();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		// Beolvassuk és létrehozzuk az adatstrukturát:
		System.out.println("Hány db klózt tartalmaz a konjuktiv normál formula?");
		int nClauses = Integer.parseInt(in.readLine());
		System.out.println("Add meg a klózokat!");
		for(int i=0;i<nClauses;i++)
		{
			String literalsList = in.readLine();
			String[] literals = literalsList.split(" ");
			Clause clause = new Clause();
			for(int i1=0;i1<literals.length;i1++)
			{
				clause.addLiteral(literals[i1]);
			}
			Clauses.add(clause);
		}
                
		if(DLL(Clauses))
		{
			System.out.println("A formula kielégíthető!");
		}
		else
		{
			System.out.println("A formula kielégíthetetlen!");
		}
		
	}
	//Az első literállal tér vissza, amit megtalál.
	static String pickLiteral(ArrayList<Clause> Clauses)
	{
		for(Clause c: Clauses)
		{
			return c.literals.get(0);
		}
		return "";
	}
	
        // Megnézi hogy a formulának van-e üres klóza.
	static boolean hasEmptyClause(ArrayList<Clause> Clauses)
	{
		for(Clause c: Clauses)
		{
			if(c.literals.size() == 0)
			{
				return true;
			}
		}
		return false;
	}
	// Kiiratás:
	static void printClauses(ArrayList<Clause> Clauses)
	{
		String formula = "{ ";
		boolean first = true;
		if(Clauses.size() == 0)formula = "{ ÜRES }";
		else
		{
			for(Clause c: Clauses)
			{
				if(first)
				{
					formula += c.printClause();
					first = false;
				}
				else formula += " && "+c.printClause();
			}
			formula += " }";
		}
		System.out.println(formula);
	}
        
        // Az algoritmus: megkap egy formulást és amig igazzal vagy hamissal nem tud visszatérni, addig tart a feldolgozása:
	static boolean DLL(ArrayList<Clause> Clauses)
	{
		while(true)
		{	
			String literalToRemove = searchSingleLiteral(Clauses);
			if(!literalToRemove.equals("Nincs")) // Van-e egységliterál
			{
				printClauses(Clauses);                               
                                
				System.out.println("Egységliterál szabály a következő változóval: "+literalToRemove);
				removeClauses(literalToRemove,Clauses); // Töröljük a klózokat, ahol egységliterállal azonos literál szerepel.
				cutClauses(literalToRemove,Clauses); // Töröljük a megmaradt klózokból az egységliterál negáltjait.
				printClauses(Clauses);
                               
				if(Clauses.size() == 0) 
				{
					System.out.println("Minden klóz törölve lett.");
					return true;
				}
				 if(hasFalsehood(Clauses)) 
				{
					System.out.println("Ellentétes előjelű egységliterálok.");
					return false;
				}
				else if(hasEmptyClause(Clauses))
				{
					System.out.println("Üres klóz.");
					return false;
				}
			}
			else
			{
				System.out.println("Nincsen egységliterál! Nem lehet az egységliterál szabályt alkalmazni.");
				break; // Ha nincs egységliterál, itt megállunk, break, és megyünk tovább.
			}
		}
		ArrayList<Clause> copy1 = new ArrayList<Clause>(); // 2 lista klózokkal
		ArrayList<Clause> copy2 = new ArrayList<Clause>();
		for(Clause c: Clauses)
		{
			Clause c2 = new Clause();
			for(String s: c.literals)
			{
				c2.addLiteral(s);
			}
			copy1.add(c2);
		}
		for(Clause c: Clauses)
		{
			Clause c2 = new Clause();
			for(String s: c.literals)
			{
				c2.addLiteral(s);
			}
			copy2.add(c2); // Mindkét listát feltöltjük a klózokkal
		}
		Clause clause1 = new Clause();
		Clause clause2 = new Clause();
		String l1 = pickLiteral(Clauses); //veszünk egy literált.
		String l2 = "";
		
		if(l1.startsWith("-")) l2 = l1.substring(1); // Ha negált, akkor az l2 lesz a nem negált.
		else l2 = "-"+l1; // Ha pedig nem negált, akkor az l2 lesz a negált.
		clause1.addLiteral(l1);
		clause2.addLiteral(l2);
		copy1.add(clause1); // Az egyik listához a nem negáltat, a másikhoz a negáltat adjuk hozzá.
		copy2.add(clause2);
		
		System.out.println("A következő klóz hozzáadás: ["+l1+"]");
		if(DLL(copy1) == true)
		{ // Ujból nézzük hogy igy mit ad a DLL algoritmus
			return true;
		}
		else
		{
			System.out.println("Próbálkozás másik klóz hozzáadásával: ["+l2+"]");
			return DLL(copy2);
		}
	}
	
        // Ez a függvény minden literált megkeres és megkeresi az ellentétes előjelű literált,ha megtalálta az egész formula hamis, és igazzal tér vissza.
	static boolean hasFalsehood(ArrayList<Clause> Clauses)
	{
		ArrayList<String> singleLiterals = new ArrayList<String>();
		for(Clause c: Clauses)
		{
			if(c.literals.size() == 1)
			{
				singleLiterals.add(c.literals.get(0));
			}
		}
		for(String sl : singleLiterals)
		{
			String sl_opposite;
			if(sl.startsWith("-")) sl_opposite = sl.substring(1);
			else sl_opposite = "-"+sl;
			for(Clause c: Clauses)
			{
				if(c.literals.size() == 1)
				{
					if(c.literals.get(0).equals(sl_opposite))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

        // Ez a függvény megkap egy literált, és törli az összes negáltját a klózokból.
	static void cutClauses(String literal,ArrayList<Clause> Clauses)
	{
		String cutLiteral;
		if(literal.startsWith("-")) cutLiteral = literal.substring(1);
		else cutLiteral = "-"+literal;
		for(Clause c: Clauses)
		{
			c.literals.remove(cutLiteral);
		}
	}
	
        // Ez a függvény törli azokat a klózokat, amelyekben a megadott literál szerepel:
	static void removeClauses(String literal,ArrayList<Clause> Clauses)
	{
		ArrayList<Clause> clausesToRemove = new ArrayList<Clause>();
		for(Clause c: Clauses)
		{
			for(String l: c.literals)
			{
				if(l.equals(literal))
				{
					clausesToRemove.add(c);
				}	
			}
		}
		for(Clause c : clausesToRemove)
		{
			Clauses.remove(c);
		}	
	}
        
        // Ez a függvény egység literált keres:
	static String searchSingleLiteral(ArrayList<Clause> Clauses)
	{
		String literalToRemove = "Nincs";
		for(Clause c: Clauses)
		{
			if(c.literals.size() == 1)
			{
				literalToRemove = c.literals.get(0);
				if(literalToRemove.startsWith("-"))
				{
					literalMap.put(literalToRemove.substring(1),false);
				}
				else
				{
					literalMap.put(literalToRemove,true);
				}
				break;
			}
		}
		return literalToRemove;
	}
}