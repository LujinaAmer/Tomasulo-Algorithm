package Tomasulo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

public class Algorithm {


	static double[] Memory = new double[1024];
	static double[][] Cache = new double[8][3];
	static String[][]  InstructionUnit = new String[1024][7];
	static long[] GeneralRegister = new long[32];
	static double[] FloatingRegister = new double[32];

	static String[] GeneralRegisterFile = new String[32];
	static String[] FloatingRegisterFile = new String[32];
	static String[][]  Add_Sub_ReservationStation;
	static String[][]  Mult_Div_ReservationStation;
	static String[][]  loadBuffer;
	static String[][]  storeBuffer;

	static String[][] BranchAddresses = new String[500][2];


	static int AddCycles;
	static int SubCycles;
	static int MulCycles;
	static int DivCycles;
	static int LoadCycles;
	static int StoreCycles;
	static int AddImmCycles=1;
	static int BranchCycles=1;



	static int ClockCycle=-1 ;
	static int InstructionNumber=0;
	static int InstructionTotal=0;

	static String[][] WriteBack = new String[1024][5]; 

	static boolean Fetched=true;
	static int FinishedExec =0;
	static int CacheFI=0;
	static boolean isBranch=false;
	static boolean isBranchTwo=false;
	static boolean Stall = false;
	static boolean StallTwo =false;

	public static void read() {

		FileReader p;

		/////////////Instruction Unit Upload
		try {
			p = new FileReader("InstructionUnit.txt");
			BufferedReader br = new BufferedReader(p);

			String line = br.readLine() ;

			int counter=-1;

			while(line != null) {
				counter++;

				String[] instruction = line.split(" ");

				if(instruction[0].charAt(instruction[0].length()-1)==':'){
					for(int i=0;i<BranchAddresses.length;i++) {
						if(BranchAddresses[i][0].equals("-")) {
							BranchAddresses[i][0]=instruction[0].substring(0, instruction[0].length()-1);
							BranchAddresses[i][1]=counter+"";
						}
					}
					InstructionUnit[counter][0]=instruction[1];
					InstructionUnit[counter][1]=instruction[2];
					InstructionUnit[counter][2]=instruction[3];
					if(instruction.length==5)
						InstructionUnit[counter][3]=instruction[4];
				}
				else {
					InstructionUnit[counter][0]=instruction[0];
					InstructionUnit[counter][1]=instruction[1];
					InstructionUnit[counter][2]=instruction[2];
					if(instruction.length==4)
						InstructionUnit[counter][3]=instruction[3];
				}
				line= br.readLine();

			}
			InstructionTotal=counter+1;

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("program in assembly language should be InstructionUnit.txt");
		}

		/////////////General Point Register Upload

		try {
			p = new FileReader("GeneralRegisterFile.txt");
			BufferedReader br = new BufferedReader(p);

			String line = br.readLine() ;

			while(line != null) {

				String[] instruction = line.split(" ");

				GeneralRegister [Integer.parseInt(instruction[0].substring(1))]=Long.parseLong(instruction[1]);


				line= br.readLine();

			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("program in assembly language should be GeneralRegisterFile.txt");
		}


		/////////////Floating Point Register Upload

		try {
			p = new FileReader("FloatingRegisterFile.txt");
			BufferedReader br = new BufferedReader(p);

			String line = br.readLine() ;

			while(line != null) {

				String[] instruction = line.split(" ");

				FloatingRegister [Integer.parseInt(instruction[0].substring(1))]=Double.parseDouble(instruction[1]);


				line= br.readLine();

			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("program in assembly language should be FloatingRegisterFile.txt");
		}

		/////////////Cache Upload
		try {
			p = new FileReader("Cache.txt");
			BufferedReader br = new BufferedReader(p);

			String line = br.readLine() ;

			int counter=-1;

			while(line != null) {

				counter++;

				String[] instruction = line.split(" ");

				Cache[counter][0]=1.0;
				Cache[counter][1]=Double.parseDouble(instruction[0]);
				Cache[counter][2]=Double.parseDouble(instruction[1]);


				line= br.readLine();

			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("program in assembly language should be Cache.txt");
		}

		try {
			p = new FileReader("Memory.txt");
			BufferedReader br = new BufferedReader(p);

			String line = br.readLine() ;


			while(line != null) {


				String[] instruction = line.split(" ");

				Memory[Integer.parseInt(instruction[0])]=Double.parseDouble(instruction[1]);


				line= br.readLine();

			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("program in assembly language should be Memory.txt");
		}

	}

	public static void instructionIssue() {


		if(!(InstructionUnit[InstructionNumber][0].equals("-"))) {

			//			System.out.println(InstructionUnit[InstructionNumber][0]+" Cycle "+ClockCycle + "Instruction " + InstructionNumber);

			if(InstructionUnit[InstructionNumber][0].equals("ADD")||InstructionUnit[InstructionNumber][0].equals("DADD")
					||InstructionUnit[InstructionNumber][0].equals("ADD.D")||InstructionUnit[InstructionNumber][0].equals("ADD.S")
					||InstructionUnit[InstructionNumber][0].equals("ADDI")||InstructionUnit[InstructionNumber][0].equals("DADDI")
					||InstructionUnit[InstructionNumber][0].equals("SUBI")||InstructionUnit[InstructionNumber][0].equals("DSUBI")
					||InstructionUnit[InstructionNumber][0].equals("BNEZ")||InstructionUnit[InstructionNumber][0].equals("SUB")
					||InstructionUnit[InstructionNumber][0].equals("DSUB")||InstructionUnit[InstructionNumber][0].equals("SUB.D")
					||InstructionUnit[InstructionNumber][0].equals("SUB.S")){

				AddSubStationFetch();
			}
			else
				if(InstructionUnit[InstructionNumber][0].equals("MUL")||InstructionUnit[InstructionNumber][0].equals("DMUL")
						||InstructionUnit[InstructionNumber][0].equals("MUL.D")||InstructionUnit[InstructionNumber][0].equals("MUL.S")
						||InstructionUnit[InstructionNumber][0].equals("DIV")||InstructionUnit[InstructionNumber][0].equals("DDIV")
						||InstructionUnit[InstructionNumber][0].equals("DIV.D")||InstructionUnit[InstructionNumber][0].equals("DIV.S")){
					MulDivStationFetch();
				}
				else
					if(InstructionUnit[InstructionNumber][0].equals("SD")||InstructionUnit[InstructionNumber][0].equals("S.D")
							||InstructionUnit[InstructionNumber][0].equals("S.S")) {

						StoreStationFetch();
					}
					else
						if(InstructionUnit[InstructionNumber][0].equals("LD")||InstructionUnit[InstructionNumber][0].equals("L.D")
								||InstructionUnit[InstructionNumber][0].equals("L.S")) {

							LoadStationFetch();
						}

		}

	}

	public static void AddSubStation() {
		Object tempResult;
		for(int i=0 ;i<Add_Sub_ReservationStation.length;i++) {


			if((Add_Sub_ReservationStation[i][0].equals("1") && !Add_Sub_ReservationStation[i][2].equals("-") && !Add_Sub_ReservationStation[i][3].equals("-"))||
					(Add_Sub_ReservationStation[i][0].equals("1") && !Add_Sub_ReservationStation[i][2].equals("-") && Add_Sub_ReservationStation[i][1].equals("BNEZ"))){

				//								System.out.println(Integer.parseInt(Add_Sub_ReservationStation[i][7]));
				//								System.out.println(InstructionUnit[4][5]);

				if(!InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5].contains("...")) {


					if(InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5].equals("-")) {

						InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]=ClockCycle+"";
						System.out.println("------------------------------------------");
						System.out.println("Instruction A" + i + " Started execution");
						System.out.println("------------------------------------------");
					}
					if(Add_Sub_ReservationStation[i][1].equals("ADD")||Add_Sub_ReservationStation[i][1].equals("DADD")){


						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]) + AddCycles) + 1 ==0) {

							InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]+"..."+(ClockCycle);

							tempResult = Long.parseLong(Add_Sub_ReservationStation[i][2])+Long.parseLong(Add_Sub_ReservationStation[i][3]); 

							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][1];
									WriteBack[j][2]=tempResult+"";
									WriteBack[j][3]=Add_Sub_ReservationStation[i][7];
									WriteBack[j][4]="A"+i;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("A"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("A"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction A" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
						else {
							System.out.println("-----------------------------------------");
							System.out.println("Instruction A" + i + " is in execution");
							System.out.println("-----------------------------------------");

						}
					}
					else if(Add_Sub_ReservationStation[i][1].equals("SUB")||Add_Sub_ReservationStation[i][1].equals("DSUB")){
						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]) + SubCycles) + 1 ==0) {

							InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]+"..."+(ClockCycle);

							tempResult = Long.parseLong(Add_Sub_ReservationStation[i][2])-Long.parseLong(Add_Sub_ReservationStation[i][3]); 

							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][1];
									WriteBack[j][2]=tempResult+"";
									WriteBack[j][3]=Add_Sub_ReservationStation[i][7];
									WriteBack[j][4]="A"+i;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("A"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("A"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction A" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
						else {
							System.out.println("-----------------------------------------");
							System.out.println("Instruction A" + i + " is in execution");
							System.out.println("-----------------------------------------");

						}
					}
					else if(Add_Sub_ReservationStation[i][1].equals("ADD.D")||Add_Sub_ReservationStation[i][1].equals("ADD.S")) {
						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]) + AddCycles) + 1 ==0) {

							InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]+"..."+(ClockCycle);

							tempResult = Double.parseDouble(Add_Sub_ReservationStation[i][2])+Double.parseDouble(Add_Sub_ReservationStation[i][3]); 
							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][1];
									WriteBack[j][2]=tempResult+"";
									WriteBack[j][3]=Add_Sub_ReservationStation[i][7];
									WriteBack[j][4]="A"+i;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("A"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("A"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction A" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
					}
					else if(Add_Sub_ReservationStation[i][1].equals("SUB.D")||Add_Sub_ReservationStation[i][1].equals("SUB.S")) {
						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]) + SubCycles) + 1 ==0) {

							InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]+"..."+(ClockCycle);

							tempResult = Double.parseDouble(Add_Sub_ReservationStation[i][2])+Double.parseDouble(Add_Sub_ReservationStation[i][3]); 
							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][1];
									WriteBack[j][2]=tempResult+"";
									WriteBack[j][3]=Add_Sub_ReservationStation[i][7];
									WriteBack[j][4]="A"+i;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("A"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("A"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction A" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
						else {
							System.out.println("-----------------------------------------");
							System.out.println("Instruction A" + i + " is in execution");
							System.out.println("-----------------------------------------");

						}
					}
					else if(Add_Sub_ReservationStation[i][1].equals("BNEZ")) {

						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]) + BranchCycles)==0) {

							InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]+"..."+(ClockCycle-1);

							boolean BranchResult= (Long.parseLong(Add_Sub_ReservationStation[i][2])!=0);

							if(BranchResult) {

								int Address = 0;
								int AddressIndex = 0;
								for(int j=0;j<BranchAddresses.length;j++) {
									if(BranchAddresses[j][0].equals(InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][2])) {
										Address = Integer.parseInt(BranchAddresses[j][1]);
										AddressIndex=j;
									}
								}


								int n=1;
								if(Address < Integer.parseInt(Add_Sub_ReservationStation[i][7])) {

									for(int j=InstructionTotal;j>Integer.parseInt(Add_Sub_ReservationStation[i][7])+1;j--) {
										for(int k=0;k<7;k++) {
											InstructionUnit[j+(Integer.parseInt(Add_Sub_ReservationStation[i][7])-Address)][k]=InstructionUnit[j-1][k];
										}
									}
									//										System.out.println(Integer.parseInt(Add_Sub_ReservationStation[i][7])+n);
									for(int j=Address;j<=Integer.parseInt(Add_Sub_ReservationStation[i][7]);j++) {
										InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])+n][0]=InstructionUnit[j][0];
										InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])+n][1]=InstructionUnit[j][1];
										InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])+n][2]=InstructionUnit[j][2];
										InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])+n][3]=InstructionUnit[j][3];
										InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])+n][4]="-";
										InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])+n][5]="-";
										InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])+n][6]="-";
										n++;
									}
									int temp = Integer.parseInt(Add_Sub_ReservationStation[i][7])+1;
									BranchAddresses[AddressIndex][1]=temp +"";
									InstructionTotal=InstructionTotal+n-1;
									//													System.out.println(InstructionUnit[1][0]+"aaaaaaa");
									//													System.out.println(Integer.parseInt(Add_Sub_ReservationStation[i][7]));
									//													System.out.println(InstructionUnit[InstructionTotal][5] + ClockCycle);
									//													System.out.println(InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])-1][0]);
								}
								else {
									FinishedExec=Address-2;
									InstructionNumber=Address;

								}

								System.out.println("------------------------------------------------------------");
								System.out.println("Instruction A" + i + " Finished execution and branch is taken");
								System.out.println("------------------------------------------------------------");
							}
							isBranch=false;
							String[] empty = {"0","-","-","-","-","-","-","-"};
							Add_Sub_ReservationStation[i]=empty;
							FinishedExec++;
							if(!BranchResult) {
								System.out.println("------------------------------------------------------------");
								System.out.println("Instruction A" + i + " Finished execution and branch is not taken");
								System.out.println("------------------------------------------------------------");
							}
							else {
								System.out.println("-----------------------------------------");
								System.out.println("Instruction A" + i + " is in execution");
								System.out.println("-----------------------------------------");

							}
						}
					}
					else if(Add_Sub_ReservationStation[i][1].equals("ADDI")||Add_Sub_ReservationStation[i][1].equals("DADDI")) {

						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]) + AddImmCycles) + 1==0) {

							InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]+"..."+(ClockCycle);

							tempResult = Long.parseLong(Add_Sub_ReservationStation[i][2])+Long.parseLong(Add_Sub_ReservationStation[i][3]); 

							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][1];
									WriteBack[j][2]=tempResult+"";
									WriteBack[j][3]=Add_Sub_ReservationStation[i][7];
									WriteBack[j][4]="A"+i;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("A"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("A"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction A" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
						else {
							System.out.println("-----------------------------------------");
							System.out.println("Instruction A" + i + " is in execution");
							System.out.println("-----------------------------------------");

						}	
					}
					else if(Add_Sub_ReservationStation[i][1].equals("SUBI")||Add_Sub_ReservationStation[i][1].equals("DSUBI")) {

						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]) + AddImmCycles) + 1==0) {

							InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]+"..."+(ClockCycle);

							tempResult = Long.parseLong(Add_Sub_ReservationStation[i][2])-Long.parseLong(Add_Sub_ReservationStation[i][3]); 

							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][1];
									WriteBack[j][2]=tempResult+"";
									WriteBack[j][3]=Add_Sub_ReservationStation[i][7];
									WriteBack[j][4]="A"+i;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("A"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("A"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction A" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
						else {
							System.out.println("-----------------------------------------");
							System.out.println("Instruction A" + i + " is in execution");
							System.out.println("-----------------------------------------");

						}	
					}
				}
			}
		}
	}

	public static void AddSubStationFetch() {


		String[] temp = {"-","-","-","-","-","-","-","-"};
		temp[0]="1";
		temp[1]=InstructionUnit[InstructionNumber][0];
		temp[7]=InstructionNumber+"";

		if(InstructionUnit[InstructionNumber][0].equals("BNEZ")) {

			if(InstructionUnit[InstructionNumber][1].charAt(0)=='R') {
				if(GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))].equals("0")) {
					temp[2]=GeneralRegister[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]+"";
				}
				else {
					temp[4]=GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]+"";
				}
			}

			if(InstructionUnit[InstructionNumber][1].charAt(0)=='F') {
				if(FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))].equals("0")) {
					temp[2]=FloatingRegister[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]+"";
				}
				else {
					temp[4]=FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]+"";
				}
			}
		}
		else {
			boolean isLabel=false;

			for(int i=0;i<BranchAddresses.length;i++) {
				if((InstructionUnit[InstructionNumber][2].equals(BranchAddresses[i][0]))) {

					isLabel=true;
				}
			}


			if(InstructionUnit[InstructionNumber][2].charAt(0)=='R' && !isLabel) {
				if(GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][2].substring(1))].equals("0")) {
					temp[2]=GeneralRegister[Integer.parseInt(InstructionUnit[InstructionNumber][2].substring(1))]+"";
				}
				else {
					temp[4]=GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][2].substring(1))]+"";
				}
			}

			if(InstructionUnit[InstructionNumber][2].charAt(0)=='F' && !isLabel) {
				if(FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][2].substring(1))].equals("0")) {
					temp[2]=FloatingRegister[Integer.parseInt(InstructionUnit[InstructionNumber][2].substring(1))]+"";
				}
				else {
					temp[4]=FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][2].substring(1))]+"";
				}
			}


			if(InstructionUnit[InstructionNumber][3].charAt(0)=='R') {

				if(GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][3].substring(1))].equals("0")) {
					temp[3]=GeneralRegister[Integer.parseInt(InstructionUnit[InstructionNumber][3].substring(1))]+"";
				}
				else {
					temp[5]=GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][3].substring(1))]+"";
				}
			}
			else
				if(InstructionUnit[InstructionNumber][3].charAt(0)=='F') {
					if(FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][3].substring(1))].equals("0")) {
						temp[3]=FloatingRegister[Integer.parseInt(InstructionUnit[InstructionNumber][3].substring(1))]+"";
					}
					else {
						temp[5]=FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][3].substring(1))]+"";
					}

				}
				else {
					temp[3]=InstructionUnit[InstructionNumber][3];

				}

		}

		for(int k=0;k<Add_Sub_ReservationStation.length;k++) {
			if(Add_Sub_ReservationStation[k][0].equals("0")) {

				if(!InstructionUnit[InstructionNumber][0].equals("BNEZ")) {
					if(InstructionUnit[InstructionNumber][1].charAt(0)=='F') {
						FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]="A"+k;
					}
					if(InstructionUnit[InstructionNumber][1].charAt(0)=='R') {
						GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]="A"+k;
					}
				}else 
				{
					if(InstructionUnit[InstructionNumber][0].equals("BNEZ")) {
						isBranch=true;
						isBranchTwo=true;
					}
				}

				Add_Sub_ReservationStation[k]=temp;
				InstructionUnit[InstructionNumber][4]=ClockCycle+"";
				System.out.println("----------------------------------------------------------------------------------");
				System.out.println("Instruction (" + temp[1] + " " + InstructionUnit[InstructionNumber][2] + " " + InstructionUnit[InstructionNumber][3] 
						+ ") is issued to Add/Sub Reservation Stations at Station A" + k );
				System.out.println("----------------------------------------------------------------------------------");


				//													for(int i=0;i<temp.length;i++) {
				//														System.out.println(temp[i]+"--"+k);
				//													}

				InstructionNumber++;
				break;
			}
		}

	}

	public static void MulDivStation() {

		Object tempResult;
		for(int i=0 ;i<Mult_Div_ReservationStation.length;i++) {
			if(Mult_Div_ReservationStation[i][0].equals("1") && !Mult_Div_ReservationStation[i][2].equals("-") && !Mult_Div_ReservationStation[i][3].equals("-")){

				if(!InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5].contains("...")) {

					if(InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5].equals("-")) {
						InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5]=ClockCycle+"";
						System.out.println("------------------------------------------");
						System.out.println("Instruction M" + i + " Started execution");
						System.out.println("------------------------------------------");
					}
					if(Mult_Div_ReservationStation[i][1].equals("MUL")||Mult_Div_ReservationStation[i][1].equals("DMUL")){
						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5]) + MulCycles) + 1 ==0) {

							InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5]=InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5]+"..."+(ClockCycle);

							tempResult = Long.parseLong(Mult_Div_ReservationStation[i][2])*Long.parseLong(Mult_Div_ReservationStation[i][3]); 

							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][1];
									WriteBack[j][2]=tempResult+"";
									WriteBack[j][3]=Mult_Div_ReservationStation[i][7];
									WriteBack[j][4]="M"+i;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("M"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("M"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction M" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
						else {
							System.out.println("-----------------------------------------");
							System.out.println("Instruction M" + i + " is in execution");
							System.out.println("-----------------------------------------");

						}
					}
					else if(Mult_Div_ReservationStation[i][1].equals("DIV")||Mult_Div_ReservationStation[i][1].equals("DDIV")){
						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5]) + DivCycles) + 1 ==0) {

							InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5]=InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5]+"..."+(ClockCycle);

							tempResult = Long.parseLong(Mult_Div_ReservationStation[i][2])/Long.parseLong(Mult_Div_ReservationStation[i][3]); 

							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][1];
									WriteBack[j][2]=tempResult+"";
									WriteBack[j][3]=Mult_Div_ReservationStation[i][7];
									WriteBack[j][4]="M"+i;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("M"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("M"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction M" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
					}
					else if(Mult_Div_ReservationStation[i][1].equals("MUL.D")||Mult_Div_ReservationStation[i][1].equals("MUL.S")) {
						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(Add_Sub_ReservationStation[i][7])][5]) + MulCycles) + 1 ==0) {

							InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5]=InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5]+"..."+(ClockCycle);

							tempResult = Double.parseDouble(Mult_Div_ReservationStation[i][2])*Double.parseDouble(Mult_Div_ReservationStation[i][3]); 
							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][1];
									WriteBack[j][2]=tempResult+"";
									WriteBack[j][3]=Mult_Div_ReservationStation[i][7];
									WriteBack[j][4]="M"+i;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("M"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("M"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction M" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
					}
					else if(Mult_Div_ReservationStation[i][1].equals("DIV.D")||Mult_Div_ReservationStation[i][1].equals("DIV.S")) {
						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5]) + DivCycles) + 1 ==0) {

							InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5]=InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][5]+"..."+(ClockCycle);

							tempResult = Double.parseDouble(Mult_Div_ReservationStation[i][2])+Double.parseDouble(Mult_Div_ReservationStation[i][3]); 
							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=InstructionUnit[Integer.parseInt(Mult_Div_ReservationStation[i][7])][1];
									WriteBack[j][2]=tempResult+"";
									WriteBack[j][3]=Mult_Div_ReservationStation[i][7];
									WriteBack[j][4]="M"+i;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("M"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("M"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction M" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
					}
				}
			}
		}
	}

	public static void MulDivStationFetch() {

		String[] temp = {"-","-","-","-","-","-","-","-"};
		temp[0]="1";
		temp[1]=InstructionUnit[InstructionNumber][0];
		temp[7]=InstructionNumber+"";

		if(InstructionUnit[InstructionNumber][2].charAt(0)=='R') {
			if(GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][2].substring(1))].equals("0")) {
				temp[2]=GeneralRegister[Integer.parseInt(InstructionUnit[InstructionNumber][2].substring(1))]+"";
			}
			else {
				temp[4]=GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][2].substring(1))]+"";
			}
		}

		if(InstructionUnit[InstructionNumber][2].charAt(0)=='F') {
			if(FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][2].substring(1))].equals("0")) {
				temp[2]=FloatingRegister[Integer.parseInt(InstructionUnit[InstructionNumber][2].substring(1))]+"";
			}
			else {
				temp[4]=FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][2].substring(1))]+"";
			}
		}

		if(InstructionUnit[InstructionNumber][3].charAt(0)=='R') {
			if(GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][3].substring(1))].equals("0")) {
				temp[3]=GeneralRegister[Integer.parseInt(InstructionUnit[InstructionNumber][3].substring(1))]+"";
			}
			else {
				temp[5]=GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][3].substring(1))]+"";
			}
		}
		else
			if(InstructionUnit[InstructionNumber][3].charAt(0)=='F') {
				if(FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][3].substring(1))].equals("0")) {
					temp[3]=FloatingRegister[Integer.parseInt(InstructionUnit[InstructionNumber][3].substring(1))]+"";
				}
				else {
					temp[5]=FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][3].substring(1))]+"";
				}

			}

			else {
				temp[3]=InstructionUnit[InstructionNumber][3];
			}

		for(int k=0;k<Mult_Div_ReservationStation.length;k++) {
			if(Mult_Div_ReservationStation[k][0].equals("0")||Mult_Div_ReservationStation[k][0].equals("-")) {

				if(InstructionUnit[InstructionNumber][1].charAt(0)=='F') {
					FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]="M"+k;
				}
				if(InstructionUnit[InstructionNumber][1].charAt(0)=='R') {
					GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]="M"+k;
				}
				Mult_Div_ReservationStation[k]=temp;
				InstructionUnit[InstructionNumber][4]=ClockCycle+"";

				System.out.println("----------------------------------------------------------------------------------");
				System.out.println("Instruction (" + temp[1]  + " " + InstructionUnit[InstructionNumber][2] + " " + InstructionUnit[InstructionNumber][3] 
						+ ") is issued to Mult/Div Reservation Stations at Station M" + k );
				System.out.println("----------------------------------------------------------------------------------");

				InstructionNumber++;
				break;
			}
		}
	}

	public static void LoadStation() {

		for(int i=0 ;i<loadBuffer.length;i++) {

			if(loadBuffer[i][0].equals("1") && !InstructionUnit[Integer.parseInt(loadBuffer[i][4])][5].contains("...")) {
				boolean flag = true;
				if( InstructionUnit[Integer.parseInt(loadBuffer[i][4])][5].equals("-")) {
					flag = false;
					InstructionUnit[Integer.parseInt(loadBuffer[i][4])][5]=ClockCycle+"";

					System.out.println("-----------------------------------------");
					System.out.println("Instruction L" + i + " Started execution");
					System.out.println("-----------------------------------------");
				}
				if(loadBuffer[i][1].equals("LD")||loadBuffer[i][1].equals("L.D")||loadBuffer[i][1].equals("L.S")){

					if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(loadBuffer[i][4])][5]) + LoadCycles) + 1 >=0) {

						double temp =0.0 ;
						boolean found =false;

						for(int j=0;j<Cache.length;j++) {
							if(Cache[j][1]==Double.parseDouble(loadBuffer[i][3])){
								found = true;
								temp=Cache[j][2];
							}
						}
						if(!found) {
							for(int j=0;j<Cache.length;j++) {
								if(Cache[j][0]==0.0){
									Cache[j][0]=1.0;
									Cache[j][1]=Double.parseDouble(loadBuffer[i][3]);
									Cache[j][2]=Memory[Integer.parseInt(loadBuffer[i][3])];
									continue;
								}
							}

							Cache[CacheFI][0]=1.0;
							Cache[CacheFI][1]=Double.parseDouble(loadBuffer[i][3]);
							Cache[CacheFI][2]=Memory[Integer.parseInt(loadBuffer[i][3])];
							CacheFI++;
							if(CacheFI==7) {
								CacheFI=0;
							}
							continue;
						}
						else {
							InstructionUnit[Integer.parseInt(loadBuffer[i][4])][5]=InstructionUnit[Integer.parseInt(loadBuffer[i][4])][5]+"..."+(ClockCycle);

							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=loadBuffer[i][2];
									if(loadBuffer[j][1].equals("LD")){
										WriteBack[j][2]=((int)temp)+"";
									}
									else {
										WriteBack[j][2]=temp+"";
									}
									WriteBack[j][3]=loadBuffer[i][4];
									WriteBack[j][4]="L"+i;
									Stall=false;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("L"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("L"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction L" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
					}
					else if(flag) {
						System.out.println("-----------------------------------------");
						System.out.println("Instruction S" + i + " is in execution");
						System.out.println("-----------------------------------------");

					}
				}
			}
		}
	}

	public static void LoadStationFetch() {

		String[] temp = {"-","-","-","-","-"};
		temp[0]="1";
		temp[1]=InstructionUnit[InstructionNumber][0];
		temp[4]=InstructionNumber+"";
		temp[2]=InstructionUnit[InstructionNumber][1];
		temp[3]=InstructionUnit[InstructionNumber][2];


		for(int k=0;k<loadBuffer.length;k++) {
			if(loadBuffer[k][0].equals("0")||loadBuffer[k][0].equals("-")) {
				boolean stall=false;
				for(int i=0;i<storeBuffer.length;i++) {
					if(storeBuffer[i][3].equals(temp[3])) {
						stall=true;
						Stall=true;
						StallTwo=true;
						break;
					}
				}
				if(!stall) {

					if(InstructionUnit[InstructionNumber][1].charAt(0)=='F') {
						FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]="L"+k;
					}
					if(InstructionUnit[InstructionNumber][1].charAt(0)=='R') {
						GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]="L"+k;
					}
					loadBuffer[k]=temp;
					InstructionUnit[InstructionNumber][4]=ClockCycle+"";

					System.out.println("----------------------------------------------------------------------------------");
					System.out.println("Instruction (" + temp[1] + " " + InstructionUnit[InstructionNumber][1] + " " + InstructionUnit[InstructionNumber][2] 
							+ ") is issued to Load Buffer at Buffer L" + k );
					System.out.println("----------------------------------------------------------------------------------");

					InstructionNumber++;
				}
				break;
			}
		}
	}

	public static void StoreStation() {


		for(int i=0 ;i<storeBuffer.length;i++) {


			if(storeBuffer[i][0].equals("1") && !storeBuffer[i][2].equals("-") && storeBuffer[i][2].charAt(0)!='L' &&
					storeBuffer[i][2].charAt(0)!='M' && storeBuffer[i][2].charAt(0)!='A' && storeBuffer[i][2].charAt(0)!='S'){

				if(!InstructionUnit[Integer.parseInt(storeBuffer[i][4])][5].contains("...")) {
					boolean flag = true;
					if(InstructionUnit[Integer.parseInt(storeBuffer[i][4])][5].equals("-")) {
						flag = false;
						InstructionUnit[Integer.parseInt(storeBuffer[i][4])][5]=ClockCycle +"";
						System.out.println("-----------------------------------------");
						System.out.println("Instruction S" + i + " Started execution");
						System.out.println("-----------------------------------------");
					}
					if(storeBuffer[i][1].equals("SD")){
						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(storeBuffer[i][4])][5]) + StoreCycles) + 1 == 0) {

							InstructionUnit[Integer.parseInt(storeBuffer[i][4])][5]=InstructionUnit[Integer.parseInt(storeBuffer[i][4])][5]+"..."+(ClockCycle);

							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=storeBuffer[i][3];
									WriteBack[j][2]=storeBuffer[i][2];
									WriteBack[j][3]=storeBuffer[i][4];
									WriteBack[j][4]="S"+i;
									Stall=false;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("S"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("S"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction S" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
						else if(flag) {
							System.out.println("-----------------------------------------");
							System.out.println("Instruction S" + i + " is in execution");
							System.out.println("-----------------------------------------");

						}
					}
					else if(storeBuffer[i][1].equals("S.D")||storeBuffer[i][1].equals("S.S")) {
						if(ClockCycle - (Integer.parseInt(InstructionUnit[Integer.parseInt(storeBuffer[i][4])][5]) + StoreCycles) + 1 ==0) {

							InstructionUnit[Integer.parseInt(storeBuffer[i][4])][5]=InstructionUnit[Integer.parseInt(storeBuffer[i][4])][5]+"..."+(ClockCycle);


							for(int j=0;j<WriteBack.length;j++) {
								if(WriteBack[j][0].equals("0")) {
									WriteBack[j][0]="1";
									WriteBack[j][1]=storeBuffer[i][3];
									WriteBack[j][2]=storeBuffer[i][2];
									WriteBack[j][3]=storeBuffer[i][4];
									WriteBack[j][4]="S"+i;
									Stall=false;
									break;
								}
							}
							String reserved = "";
							for(int j = 0; j < GeneralRegisterFile.length; j++) {
								if(GeneralRegisterFile[j].equals("S"+i)) {
									reserved = "R" + j;
								}
							}
							for(int j = 0; j < FloatingRegisterFile.length; j++) {
								if(FloatingRegisterFile[j].equals("S"+i)) {
									reserved = "F" + j;
								}
							}
							System.out.println("------------------------------------------------------------");
							System.out.println("Instruction S" + i + " Finished execution and " + reserved + " is now ready");
							System.out.println("------------------------------------------------------------");
						}
						else if(flag) {
							System.out.println("-----------------------------------------");
							System.out.println("Instruction S" + i + " is in execution");
							System.out.println("-----------------------------------------");

						}
					}
				}
			}
		}
	}

	public static void StoreStationFetch() {


		String[] temp = {"-","-","-","-","-"};
		temp[0]="1";
		temp[1]=InstructionUnit[InstructionNumber][0];
		temp[4]=InstructionNumber+"";

		if(InstructionUnit[InstructionNumber][1].charAt(0)=='R') {
			if(GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))].equals("0")) {
				temp[2]=GeneralRegister[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]+"";
			}
			else {
				temp[2]=GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]+"";
			}
		}

		if(InstructionUnit[InstructionNumber][1].charAt(0)=='F') {
			if(FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))].equals("0")) {
				temp[2]=FloatingRegister[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]+"";
			}
			else {
				temp[2]=FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]+"";
			}
		}

		temp[3]=InstructionUnit[InstructionNumber][2];

		for(int k=0;k<storeBuffer.length;k++) {
			if(storeBuffer[k][0].equals("0")||storeBuffer[k][0].equals("-")) {

				boolean stall=false;

				for(int i=0;i<storeBuffer.length;i++) {
					if(storeBuffer[i][3].equals(temp[3])) {
						stall=true;
						Stall=true;
						StallTwo=true;
						break;
					}
				}
				for(int i=0;i<loadBuffer.length;i++) {
					if(loadBuffer[i][3].equals(temp[3])) {
						stall=true;
						Stall=true;
						StallTwo=true;
						break;
					}
				}
				if(!stall) {


					if(InstructionUnit[InstructionNumber][1].charAt(0)=='F') {
						FloatingRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]="S"+k;
					}
					if(InstructionUnit[InstructionNumber][1].charAt(0)=='R') {
						GeneralRegisterFile[Integer.parseInt(InstructionUnit[InstructionNumber][1].substring(1))]="S"+k;
					}
					storeBuffer[k]=temp;
					InstructionUnit[InstructionNumber][4]=ClockCycle+"";

					System.out.println("----------------------------------------------------------------------------------");
					System.out.println("Instruction (" + temp[1]+ " " + InstructionUnit[InstructionNumber][1] + " " + InstructionUnit[InstructionNumber][2] 
							+ ") is issued to Store Buffer at Buffer S" + k );
					System.out.println("----------------------------------------------------------------------------------");

					InstructionNumber++;
				}
				break;
			}
		}
	}


	public static void WriteBack() {

		int minInstruction=Integer.MAX_VALUE;
		int minInstructionIndex=-1;

		for(int i=0;i<WriteBack.length;i++) {
			if( WriteBack[i][0].equals("1") && Integer.parseInt(WriteBack[i][3])<minInstruction) {
				minInstruction = Integer.parseInt(WriteBack[i][3]);
				minInstructionIndex=i;
			}
		}

		if(minInstructionIndex!=-1) {
			FinishedExec++;

			if(WriteBack[minInstructionIndex][1].charAt(0)=='F') {

				if(FloatingRegisterFile[Integer.parseInt((WriteBack[minInstructionIndex][1].substring(1)))].equals(WriteBack[minInstructionIndex][4])) {

					FloatingRegisterFile[Integer.parseInt((WriteBack[minInstructionIndex][1].substring(1)))]=0+"";
					FloatingRegister[Integer.parseInt((WriteBack[minInstructionIndex][1].substring(1)))]=Double.parseDouble((WriteBack[minInstructionIndex][2]));
				}

				InstructionUnit[minInstruction][6]=ClockCycle+"";

				String[] temp = {"0","-","-","-","-","-","-","-"};
				if((WriteBack[minInstructionIndex][4].charAt(0)=='A')) {
					Add_Sub_ReservationStation[Integer.parseInt((WriteBack[minInstructionIndex][4]).substring(1))]=temp;
				}
				if((WriteBack[minInstructionIndex][4].charAt(0)=='M')) {
					Mult_Div_ReservationStation[Integer.parseInt((WriteBack[minInstructionIndex][4]).substring(1))]=temp;
				}
				String[] tempLoad= {"0","-","-","-","-"};
				if((WriteBack[minInstructionIndex][4].charAt(0)=='L')) {
					loadBuffer[Integer.parseInt((WriteBack[minInstructionIndex][4]).substring(1))]=tempLoad;
				}
			}


			else if(WriteBack[minInstructionIndex][1].charAt(0)=='R') {

				if(GeneralRegisterFile[Integer.parseInt((WriteBack[minInstructionIndex][1].substring(1)))].equals(WriteBack[minInstructionIndex][4])) {

					GeneralRegisterFile[Integer.parseInt((WriteBack[minInstructionIndex][1].substring(1)))]=0+"";
					if((WriteBack[minInstructionIndex][2]).contains("."))
						GeneralRegister[Integer.parseInt((WriteBack[minInstructionIndex][1].substring(1)))]=(int)Double.parseDouble((WriteBack[minInstructionIndex][2]));
					else
						GeneralRegister[Integer.parseInt((WriteBack[minInstructionIndex][1].substring(1)))]=(int)Integer.parseInt((WriteBack[minInstructionIndex][2]));
				}

				InstructionUnit[minInstruction][6]=ClockCycle+"";

				String[] temp = {"0","-","-","-","-","-","-","-"};
				if((WriteBack[minInstructionIndex][4].charAt(0)=='A')) {
					Add_Sub_ReservationStation[Integer.parseInt((WriteBack[minInstructionIndex][4]).substring(1))]=temp;
				}
				if((WriteBack[minInstructionIndex][4].charAt(0)=='M')) {
					Mult_Div_ReservationStation[Integer.parseInt((WriteBack[minInstructionIndex][4]).substring(1))]=temp;
				}
				String[] tempLoad= {"0","-","-","-","-"};
				if((WriteBack[minInstructionIndex][4].charAt(0)=='L')) {
					loadBuffer[Integer.parseInt((WriteBack[minInstructionIndex][4]).substring(1))]=tempLoad;
				}
			}


			else if((WriteBack[minInstructionIndex][4].charAt(0)=='S')) {
				String[] tempStore= {"0","-","-","-","-"};
				Memory[Integer.parseInt(WriteBack[minInstructionIndex][1])]=Double.parseDouble(WriteBack[minInstructionIndex][2]);
				storeBuffer[Integer.parseInt((WriteBack[minInstructionIndex][4]).substring(1))]=tempStore;
				InstructionUnit[minInstruction][6]=ClockCycle+"";
			}



			for(int i=0;i<Add_Sub_ReservationStation.length;i++) {
				if(Add_Sub_ReservationStation[i][4].equals(WriteBack[minInstructionIndex][4])) {
					Add_Sub_ReservationStation[i][2]=WriteBack[minInstructionIndex][2];
					Add_Sub_ReservationStation[i][4]="-";
				}
				else
					if(Add_Sub_ReservationStation[i][5].equals(WriteBack[minInstructionIndex][4])) {
						Add_Sub_ReservationStation[i][3]=WriteBack[minInstructionIndex][2];
						Add_Sub_ReservationStation[i][5]="-";
					}
			}

			for(int i=0;i<Mult_Div_ReservationStation.length;i++) {
				if(Mult_Div_ReservationStation[i][4].equals(WriteBack[minInstructionIndex][4])) {
					Mult_Div_ReservationStation[i][2]=WriteBack[minInstructionIndex][2];
					Mult_Div_ReservationStation[i][4]="-";
				}
				else
					if(Mult_Div_ReservationStation[i][5].equals(WriteBack[minInstructionIndex][4])) {
						Mult_Div_ReservationStation[i][3]=WriteBack[minInstructionIndex][2];
						Mult_Div_ReservationStation[i][5]="-";
					}
			}

			for(int i=0;i<storeBuffer.length;i++) {
				if(storeBuffer[i][2].equals(WriteBack[minInstructionIndex][4])) {
					storeBuffer[i][2]=WriteBack[minInstructionIndex][2];
				}
			}



			String[] temp = {"0","-","-","-","-"}; 
			WriteBack[minInstructionIndex]=temp;
		}

	}


	public static void printbuffers() {
		System.out.println("Add/Sub Reservation Station");
		System.out.println();
		String[][] x = Add_Sub_ReservationStation;
		for(int i = 0; i < Add_Sub_ReservationStation.length; i++) {
			System.out.println(" -Name- " + " -busy- " + " -OP- " + " -Vj- " + " -Vk- " + " -Qj- " + " -Qk- " + " -no.- ");
			System.out.println(" - " + "A" + i + " - " + " -  " + x[i][0] + " - " + " -" + x[i][1] + "- " + " -" + x[i][2] + "- " + " -" + x[i][3] + " - " + " -" + x[i][4] + "- " + "  -" + x[i][5] + "- " + "  -" + x[i][7] + "- ");
			System.out.println("-------------------------------------------------");
		}
		System.out.println();
		System.out.println("Mul/Div Reservation Station");
		System.out.println();
		x = Mult_Div_ReservationStation;
		for(int i = 0; i < Mult_Div_ReservationStation.length; i++) {
			System.out.println(" -Name- " + " -busy- " + " -OP- " + " -Vj- " + " -Vk- " + " -Qj- " + " -Qk- " + " -no.- ");
			System.out.println(" - " + "M" + i + " - " + " -  " + x[i][0] + " - " + " -" + x[i][1] + "- " + " -" + x[i][2] + "- " + " -" + x[i][3] + " - " + " -" + x[i][4] + "- " + "  -" + x[i][5] + "- " + "  -" + x[i][7] + "- ");
			System.out.println("-------------------------------------------------");
		}
		System.out.println();
		System.out.println("Load Buffer");
		System.out.println();
		x = loadBuffer;
		for(int i = 0; i < loadBuffer.length; i++) {
			System.out.println(" -Name- " + " -busy- " + " -OP- " + " -Dest- " + " -Add- " + " -no.- ");
			System.out.println(" - " + "L" + i + " - " + " -  " + x[i][0] + " - " + " -" + x[i][1] + "- " + "   -" + x[i][2] + "-  " + "   -" + x[i][3] + "- " + "   -" + x[i][4] + "- ");
			System.out.println("-------------------------------------------------");
		}
		System.out.println();
		System.out.println("Store Buffer");
		System.out.println();
		x = storeBuffer;
		for(int i = 0; i < storeBuffer.length; i++) {
			System.out.println(" -Name- " + " -busy- " + " -OP- " + " -Dest- " + " -Add- " + " -no.- ");
			System.out.println(" - " + "L" + i + " - " + " -  " + x[i][0] + " - " + " -" + x[i][1] + "- " + "   -" + x[i][2] + "-  " + "   -" + x[i][3] + "- " + "   -" + x[i][4] + "- ");
			System.out.println("-------------------------------------------------");
		}
		System.out.println();
		System.out.println("General Register File");
		System.out.println();
		System.out.print("[ ");
		long[] y = GeneralRegister;
		for(int i = 1; i < GeneralRegister.length; i++) {
			if(GeneralRegisterFile[i].equals("0")) {
				System.out.print("{R" + i + " : " + y[i] + "}, ");
			}
			else {
				System.out.print("{R" + i + " : " + GeneralRegisterFile[i] + "}, ");
			}
			if(i%12==0) {
				System.out.println();
			}
		}
		System.out.println(" ]");
		System.out.println();
		System.out.println("Float Register File");
		System.out.println();
		System.out.print("[ ");
		double[] z = FloatingRegister;
		for(int i = 1; i < FloatingRegister.length; i++) {
			if(FloatingRegisterFile[i].equals("0")) {
				System.out.print("{F" + i + " : " + z[i] + "}, ");
			}
			else {
				System.out.print("{F" + i + " : " + FloatingRegisterFile[i] + "}, ");
			}
			if(i%10==0) {
				System.out.println();
			}
		}
		System.out.println(" ]");
		System.out.println();
		System.out.println("Cache");
		System.out.println();
		double[][] k = Cache;
		for(int i = 0; i < Cache.length; i++) {
			System.out.println(" -Index- " + " -busy- " + " -Add- " + " -Value-");
			System.out.println(" -  " + i + "  - " + " - " + k[i][0] + " -" + " -" + k[i][1] + "- " + "  -" + k[i][2] + "-");
			System.out.println("-------------------------------------------------");
		}
		System.out.println();
		System.out.println();
		System.out.println("Queue");
		System.out.println();
		String[][] Q = InstructionUnit;
		for(int i = InstructionNumber; i < InstructionTotal; i++) {
			System.out.println(" -OP- " + " -DestR- " + " -SrcSR- " + " -SrcTR-");
			System.out.println(" -" + Q[i][0] + "- " + "--" + Q[i][1] + " --" + "  --" + Q[i][2] + " --" + "  --" + Q[i][3] + " --");
			System.out.println("-------------------------------------------------");
		}
		System.out.println();
		System.out.println();
		System.out.println("Time table");
		System.out.println();
		for(int i = 0; i < InstructionNumber; i++) {
			for(int j=0;j<7 ;j++) {
				System.out.print(InstructionUnit[i][j]+"-");
			}
			System.out.println();
		}
		System.out.println();

	}

	public static void start() {
		while(FinishedExec<InstructionTotal){
			ClockCycle++;
			System.out.println("At Clock Cycle: " + ClockCycle);
			if(ClockCycle==0) {
				if(!isBranch && !Stall) {
					System.out.println("ff");
					instructionIssue();
					System.out.println("aff");
				}
			}
			else {
				System.out.println("bex");
				WriteBack();
				AddSubStation();
				MulDivStation();
				StoreStation();
				LoadStation();
				System.out.println("aex");

				if(!isBranch) {
					if(!isBranchTwo && !Stall) {
						System.out.println("off");
						instructionIssue();
						System.out.println("aoff");
					}
				}

				if(!isBranch && !Stall) {
					if(isBranchTwo || StallTwo ) {
						System.out.println("fff");
						instructionIssue();
						isBranchTwo=false;
						StallTwo=false;
						System.out.println("afff");

					}
				}

			}
			printbuffers();
			System.out.println("------------------------------------------------------------------------------------------------------------------");
		}
	}

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		System.out.println("Please enter the Add/Sub Reservation Station size");
		int AddSubSize = sc.nextInt();
		System.out.println("Please enter the Mul/Div Reservation Station size");
		int MulDivSize = sc.nextInt();
		System.out.println("Please enter the Load Buffer size");
		int LoadSize = sc.nextInt();
		System.out.println("Please enter the Store Buffer size");
		int StoreSize = sc.nextInt();

		Add_Sub_ReservationStation =new String[AddSubSize][8];
		Mult_Div_ReservationStation =new String[MulDivSize][8];
		loadBuffer =new String[LoadSize][5];
		storeBuffer =new String[StoreSize][5];

		System.out.println("Please enter Add latency");
		AddCycles = sc.nextInt();
		System.out.println("Please enter Sub latency");
		SubCycles = sc.nextInt();
		System.out.println("Please enter Mul latency");
		MulCycles = sc.nextInt();
		System.out.println("Please enter Div latency");
		DivCycles = sc.nextInt();
		System.out.println("Please enter Load latency");
		LoadCycles = sc.nextInt();
		System.out.println("Please enter Store latency");
		StoreCycles = sc.nextInt();









		for(int i=0;i<Add_Sub_ReservationStation.length;i++) {
			String[] temp = {"0","-","-","-","-","-","-","-"};
			Add_Sub_ReservationStation[i]= temp;
		}
		for(int i=0;i<Mult_Div_ReservationStation.length;i++) {
			String[] temp = {"0","-","-","-","-","-","-","-"};
			Mult_Div_ReservationStation[i]= temp;
		}
		for(int i=0;i<loadBuffer.length;i++) {
			String[] temp = {"0","-","-","-","-"};
			loadBuffer[i]= temp;
		}
		for(int i=0;i<storeBuffer.length;i++) {
			String[] temp = {"0","-","-","-","-"};
			storeBuffer[i]= temp;
		}
		for(int i=0;i<WriteBack.length;i++) {
			String[] temp = {"0","-","-","-","-"};
			WriteBack[i]= temp;
		}
		for(int i=0;i<BranchAddresses.length;i++) {
			String[] temp = {"-","-"};
			BranchAddresses[i]= temp;
		}
		for(int i=0;i<GeneralRegisterFile.length;i++) {
			String temp = "0";
			GeneralRegisterFile[i]= temp;
		}
		for(int i=0;i<FloatingRegisterFile.length;i++) {
			String temp = "0";
			FloatingRegisterFile[i]= temp;
		}
		for(int i=0;i<InstructionUnit.length;i++) {
			String[] temp = {"-","-","-","-","-","-","-"};
			InstructionUnit[i]= temp;
		}
		for(int i=0;i<Cache.length;i++) {
			double[] temp = {0.0,0.0,0.0};
			Cache[i]= temp;
		}
		for(int i=0;i<GeneralRegister.length;i++) {
			int temp = 0;
			GeneralRegister[i]= temp;
		}
		for(int i=0;i<FloatingRegister.length;i++) {
			double temp = 0.0;
			FloatingRegister[i]= temp;
		}
		for(int i=0;i<Memory.length;i++) {
			double temp = 0.0;
			Memory[i]= temp;
		}


		read();
		start();


	}
}
