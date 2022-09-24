package cds.savot.samples;

import cds.savot.binary.DataBinaryReader;
import cds.savot.binary2.DataBinary2Reader;
import cds.savot.model.FieldSet;
import cds.savot.model.SavotBinary2;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;

import java.io.File;
import java.util.Date;
import java.util.Objects;

/**
 * Tests the parsing of a "binary" VOTable by Savot + DataBinaryReader.
 * <p>
 * The Savot parser used is SavotPullParser in mode SEQUENTIAL.
 *
 * @author Gregory Mantelet (CDS)
 * @version 21/09/2011
 * @see DataBinaryReader
 */
public class VotableReader_SavotBinary2 {
    //SavotStaxParser sb = null;
    SavotPullParser sb = null;

    public static void main(String[] args) throws Exception {
        VotableReader_SavotBinary2 parser = new VotableReader_SavotBinary2();
        parser.parse(VOTABLE_PATH);
    }

    // REQUIRED PARAMETERS:
    private static final String PARENT_DIRECTORY = "/Users/andre/eclipse-workspace2/Savot/VOTable-Tests-V4.0";
    private static final String VOTABLE_PATH = PARENT_DIRECTORY + File.separatorChar + "esaGaiaColumnsMeta.vot";

    // OPTIONAL PARAMETERS:
    /**
     * Path of the file in which decoded data must be written. "null" means System.out.
     */
    private static final String DATA_OUTPUT = null;

    public VotableReader_SavotBinary2() {
        ;
    }

    public void parse(final String fileOrUrl) throws Exception {
//		System.out.println("Parsing begin ");
        Date dates = new Date();

        // begin the parsing
        //sb = new SavotStaxParser(fileOrUrl, SavotPullEngine.FULL);

        sb = new SavotPullParser(fileOrUrl, SavotPullEngine.FULL);


        //	System.out.println("item count : " + sb.getAllResources().getResources().getItemCount() );

        Date datef = new Date();
        //	System.out.println("Parsing ends with a duration of " + ((datef.getHours()*3600 + datef.getMinutes()*60 + datef.getSeconds()) - (dates.getHours()*3600 + dates.getMinutes()*60 + dates.getSeconds())) + " s");

        for (int item = 0; item < sb.getAllResources().getResources().getItemCount(); item++) {
            SavotBinary2 binary2 = Objects.requireNonNull(sb.getAllResources().getResources().getItemAt(item)).getData(0).getBinary2();
            //		if (binary2 != null)
            //			System.out.println(binary2.getStream().getContent());

            if (binary2 != null) {
                FieldSet fields = Objects.requireNonNull(sb.getAllResources().getResources().getItemAt(item)).getFieldSet(0);
		/*		
				int nbSignBytes = (fields.getItemCount() + 7) / 8;
				
				System.out.println("le nb de Fiels est: " + fields.getItemCount() + " et donc le nb de Byte de signe est: " + nbSignBytes);
				for (int identB=0; identB < nbSignBytes; identB++) {
					SavotField lesnuls = new SavotField();
					lesnuls.setDataType("char");
					lesnuls.setName("SignByte" + identB);
					fields.set.add(identB,lesnuls);
				}
				System.out.println("le nb de Fiels est: " + fields.getItemCount() + " et donc le nb de Byte de signe est: " + nbSignBytes);

				System.out.println("le nb de Fields est: " + fields.getItemCount());
*/
                DataBinary2Reader parser = new DataBinary2Reader(binary2.getStream(), fields, false, PARENT_DIRECTORY);

//				PrintStream output = (DATA_OUTPUT != null) ? new PrintStream(DATA_OUTPUT) : System.out;
                //	output.println("\n*** DATA ***");
                int jj = 2;
                parser.next();
                int nb = 0;
//		System.out.println("avant parser le nb de Fields est: " + fields.getItemCount());

                while (parser.next() && nb < 10) {
                    //			output.print("-> ****** NEW FIELD ****** from :" + fields.getItemCount() + "*****");
                    for (int j = 0; j < fields.getItemCount(); j++) {
                        //if (parser.isCellNull(j)) System.out.print("!!!!! Bit de nullité à 1 !!!!");
                        //	output.print("Field " + j + " name: " + fields.getItemAt(j).getName() + " " + parser.getCellAsString(j) + " ; ");
                    }
                    //		output.println();
                    nb++;
                }
            } else
                System.out.println("no binary2");
        }
		
/*		for (int i = 0 ; i < sb.getAllResources().getResources().getItemAt(0).getTables().getItemCount(); i++) {
			System.out.print(sb.getAllResources().getResources().getItemAt(0).getTables().getItemAt(i).getName() + " avec le nb de Fields : ");
			System.out.print(sb.getAllResources().getResources().getItemAt(0).getTables().getItemAt(i).getFields().getItemCount());
			System.out.println(" et de nom : " + sb.getAllResources().getResources().getItemAt(0).getTables().getItemAt(i).getFields().getItemAt(0).getName());
			//System.out.println(" et de nb de lignes : " + sb.getAllResources().getResources().getItemAt(0).getTables().getItemAt(i).getData().getTableData().getTRs().getItemCount());
			//System.out.println(" ayant chacune nb data : " + sb.getAllResources().getResources().getItemAt(0).getTables().getItemAt(i).getData().getTableData().getTRs().getItemAt(0).getTDs().getItemCount());			
	//		System.out.println(parser.getAllResources().getInfos().getItemAt(0).getContent());			

		}*/
    }
}
