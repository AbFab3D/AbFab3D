/**
   calculates DataSource value at the given point 
   pScene - scene description, contains opcodes and data needed for calculations
   pnt - input point 
   result - output data value 
 */
void getShapeJSData(Scene *pScene, sVec *pnt, sVec *result) {
    
    CPtr ptr;  // pointer to opcodes struct to convert from int* to structs* 
    int offsetIn = 0;  // current offset in the opcodes 

    sVec stack[STACK_SIZE];
    int stackPos = 0; // current stack position
    
    //int popcode[MAXOPSIZE]; // private opcode, no benefits so far 

    PTROPS int *opcode = pScene->pOps; 
    int opCount = pScene->opCount;

    sVec pnt1;   // current working point 
    
    sVec data1 = (sVec){.v=(float4)(0,0,0,0)}; // register to store current data value
    sVec data2;  // register for intermediate data 

    // original point 
    pnt1 = *pnt;
    
    for(int i=0; i < opCount; i++) {
        int size = opcode[offsetIn];

        if(size <= 0)
            break;
        
        int code = opcode[offsetIn+1];
        ptr.w = (opcode + offsetIn);
        
        /*
        // copy data into local makes things slower 
        for(int k = 0; k < size/4; k++){
            popcode[k] = opcode[offsetIn+k];
        }
        int code = popcode[1];
        ptr.w = popcode;
        */

        switch(code){
        default:

            *result = data1;
            return;            

        case oSPHERE:
            
            oSphere(ptr.pv, &pnt1, &data1);
            break;
            
        case oGYROID:     
            oGyroid(ptr.pv, &pnt1, &data1);        
            break;

        case oSCHWARZP:
            
            oSchwarzP(ptr.pv, &pnt1, &data1);        
            break;

        case oSCHWARZD:
            
            oSchwarzD(ptr.pv, &pnt1, &data1);        
            break;

        case oLIDINOID:
            
            oLidinoid(ptr.pv, &pnt1, &data1);        
            break;

        case oBOX:
            
            oBox(ptr.pv, &pnt1, &data1);
            break;

        case oGRID2DBYTE:
            
            oGrid2dByte(ptr.pv, &pnt1, &data1, pScene);
            break;

        case oIMAGE3D:
            
            oImage3D(ptr.pv, &pnt1, &data1, pScene);
            break;

        case oGRID3DBYTE:
            
            oGrid3dByte(ptr.pv, &pnt1, &data1, pScene);
            break;

        case oTORUS:
            
            oTorus(ptr.pv, &pnt1, &data1);
            break;

        case oCOPY_D1D2:
            
            data2 = data1;
            break;            
            
        case oCOPY_D2D1:
            
            data1 = data2;
            break;
                                    
        case oMAX:
            
            oMax(ptr.pv,&data2, &data1,&data2);
            break;
            
        case oMIN:
            
            oMin(ptr.pv,&data2, &data1,&data2);
            break;
            
        case oBLENDMIN:            

            oBlendMin(ptr.pv, &data1,&data2, &data2);            
            break;
            
        case oBLENDMAX:
            
            oBlendMax(ptr.pv, &data1,&data2, &data2);        
            break;
            
        case oSUBTRACT:
            
            oSubtract(ptr.pv,&data1, &data2,&data2);            
            break;
            
        case oBLENDSUBTRACT:
            
            oBlendSubtract(ptr.pv,&data1, &data2,&data2);
            break;            

        case oEMBOSSING:
            
            oEmbossing(ptr.pv,&data1, &data2,&data2);
            break;            

        case oPUSH_D2:
            
            stack[stackPos++] = data2;
            break;            
        case oPOP_D2:
            
            data2 = stack[--stackPos];            
            break;

        case oPUSH_P1:
            
            stack[stackPos++] = pnt1;
            break;            
        case oPOP_P1:
            
            pnt1 = stack[--stackPos];            
            break;

        case oTRANSLATION:
            
            oTranslation(ptr.pv,&pnt1);
            break;         

        case oROTATION:
            
            oRotation(ptr.pv,&pnt1);
            break;            

        case oSCALE:
            
            oScale(ptr.pv,&pnt1);
            break;            

        case oNOISE3D:
            
            oNoise3D(ptr.pv, &pnt1, &data1, pScene);
            break;


        }
        offsetIn += size;
    }

    *result = data1;
    return;            
}

