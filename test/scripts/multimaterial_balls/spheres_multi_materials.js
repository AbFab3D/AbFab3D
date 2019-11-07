var uiParams = [
    {name: "sceneSize", type: "double", defaultVal: 50,unit: "MM"},
    {name: "radius", type: "double", rangeMin: 1,step: 1,defaultVal: 5,unit: "MM"},
    {name: "period", type: "double", rangeMin: 1,rangeMax: 21, step: 1, defaultVal:18,unit: "MM"},
    {name: "enable0",type: "boolean",defaultVal: true},
    {name: "enable1",type: "boolean",defaultVal: true},
    {name: "enable2",type: "boolean",defaultVal: true},
    
    {name: "thickness0",type: "double",defaultVal: 5,unit: "MM" },
    {name: "thickness1",type: "double",defaultVal: 1,unit: "MM" },
    {name: "thickness2",type: "double",defaultVal: 2,unit: "MM" },
    
    {name: "tc0_R",type: "double",defaultVal: 0,unit: "MM",group:"transmission" },
    {name: "tc0_G",type: "double",defaultVal: 0,unit: "MM",group:"transmission"  },
    {name: "tc0_B",type: "double",defaultVal: 0,unit: "MM",group:"transmission"  },
    {name: "tc1_R",type: "double",defaultVal: 0,unit: "MM",group:"transmission"  },
    {name: "tc1_G",type: "double",defaultVal: 0,unit: "MM",group:"transmission"  },
    {name: "tc1_B",type: "double",defaultVal: 0,unit: "MM",group:"transmission"  },
    {name: "tc2_R",type: "double",defaultVal: 0,unit: "MM",group:"transmission"  },
    {name: "tc2_G",type: "double",defaultVal: 0,unit: "MM",group:"transmission"  },
    {name: "tc2_B",type: "double",defaultVal: 0,unit: "MM",group:"transmission"  },
    {name: "shininess0",type: "double",defaultVal: 0, group:"shininess"},
    {name: "shininess1",type: "double",defaultVal: 0, group:"shininess"},
    {name: "shininess2",type: "double",defaultVal: 0, group:"shininess"},
    {name: "albedo0R",type: "double",defaultVal: 0, group:"shininess"},
    {name: "albedo0G",type: "double",defaultVal: 0, group:"shininess"},
    {name: "albedo0B",type: "double",defaultVal: 0, group:"shininess"},
    {name: "albedo1R",type: "double",defaultVal: 0, group:"shininess"},
    {name: "albedo1G",type: "double",defaultVal: 0, group:"shininess"},
    {name: "albedo1B",type: "double",defaultVal: 0, group:"shininess"},
    {name: "albedo2R",type: "double",defaultVal: 0, group:"shininess"},
    {name: "albedo2G",type: "double",defaultVal: 0, group:"shininess"},
    {name: "albedo2B",type: "double",defaultVal: 0, group:"shininess"},
    
    {name: "tracingDeph",type: "integer",defaultVal: 1},
    {name: "maxIntersectrions",type: "integer",defaultVal: 1},
    {name: "surfaceJump",label:"surfaceJump(mm)",type: "double",defaultVal: 1,unit: "MM" },
    
];


function main(args) {
  
    var radius = args.radius;
    var period = args.period;
    var boxSize  = 0.8*period;
    var boxDepth = 0.5*boxSize;
    var tc0 = new Vector3d(args.tc0_R,args.tc0_G,args.tc0_B);
    var tc1 = new Vector3d(args.tc1_R,args.tc1_G,args.tc1_B);
    var tc2 = new Vector3d(args.tc2_R,args.tc2_G,args.tc2_B);
    var albedo0 = new Color(args.albedo0R,args.albedo0G,args.albedo0B);
    var albedo1 = new Color(args.albedo1R,args.albedo1G,args.albedo1B);
    var albedo2 = new Color(args.albedo2R,args.albedo2G,args.albedo2B);
    
    var th0 = args.thickness0;
    var th1 = args.thickness1;
    var th2 = args.thickness2;
    
    var sphere1 = new Sub(new Abs(new Sphere(new Vector3d(0,0,0), radius-th0/2)),th0/2);
    var sphere2 = new Sub(new Abs(new Sphere(new Vector3d(2*radius,0,0), radius-th1/2)),th1/2);
    var sphere3 = new Sphere(new Vector3d(-2*radius, 0,0), radius);
    var sphere4 = new Sphere(new Vector3d(0,2*radius,0), radius);
    var sphere5 = new Sphere(new Vector3d(0,-2*radius,radius), radius);
    //var sphere5 = new Sub(new Abs(new Sphere(new Vector3d(radius,0,2*radius), radius)),th1/2);
                
    var wrap = new PeriodicWrap(new Vector3d(period, 0,0),new Vector3d(0,period, 0));
    wrap.setOrigin(new Vector3d(-period/2, -period/2,0));
    var box = new Box(boxSize, boxSize, boxDepth);
    box.set("rounding", 1*MM);
    box.addTransform(wrap);
    box.addTransform(new Translation(new Vector3d(0,0,-radius-boxDepth)));
        
    var pink = new SingleColorMaterial(1,0.,0.3);
    pink.setShaderParam("shininess", args.shininess0);
    pink.setShaderParam("transmittanceCoeff", tc0);
    
    var green = new SingleColorMaterial(0.,0.9,0.);
    green.setShaderParam("shininess", args.shininess1);
    green.setShaderParam("transmittanceCoeff", tc1);
    
    var blue = new SingleColorMaterial(0.,0.1,0.9);
    blue.setShaderParam("transmittanceCoeff", tc2);
    blue.setShaderParam("shininess", args.shininess2);
    blue.setShaderParam("albedo", albedo2);
    var gray = new SingleColorMaterial(0.7,0.7,0.7);
    gray.setShaderParam("shininess", 0.5);
    
    var s = args.sceneSize/2;
    var scene = new Scene(new Bounds(-s,s,-s,s,-s,s));

    if(args.enable0 == true)scene.addShape(new Shape(sphere1, pink));
    if(args.enable1 == true)scene.addShape(new Shape(sphere2, green));
    if(args.enable2 == true){
      scene.addShape(new Shape(sphere3, blue));
      scene.addShape(new Shape(sphere4, blue));
      scene.addShape(new Shape(sphere5, blue));
    }
    scene.addShape(new Shape(box, gray));
    
    return scene;
}
