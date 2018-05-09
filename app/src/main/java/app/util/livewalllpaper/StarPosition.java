package app.util.livewalllpaper;

public class StarPosition
 {
  //kakusan
  public static final double refraction = 0.58556;


  //
  public static double getSunEclipticLongitude(double T)
   {
    double result=280.4603 + 360.00769 * T +
 		    (1.9146 - 0.00005 * T) * Math.sin((357.538 + 359.991 * T)  / 180.0 * Math.PI) +
			 0.02 * Math.sin((355.05 + 719.981 * T)  / 180.0 * Math.PI) +
			 0.0048 * Math.sin((234.95 + 19.341 * T) / 180.0 * Math.PI) +
			 0.002 * Math.sin((247.1 + 329.64 * T) / 180.0 * Math.PI) +
			 0.0018 * (Math.sin((297.8 + 4452.67 * T) / 180.0 * Math.PI) + Math.sin((251.3 + 0.2 * T) / 180.0 * Math.PI)) +
			 0.0015 * Math.sin((343.2 + 450.37 * T)  / 180.0 * Math.PI) +
			 0.0013 * Math.sin((81.4 + 225.18 * T)  / 180.0 * Math.PI) +
			 0.0008 * Math.sin((132.5 + 659.29 * T)  / 180.0 * Math.PI) +
			 0.0007 * (Math.sin((153.3 + 90.38 * T) / 180.0 * Math.PI) + Math.sin((206.8 + 30.35 * T) / 180.0 * Math.PI)) +
			 0.0006 * Math.sin((29.8 + 337.18 * T)  / 180.0 * Math.PI) +
			 0.0005 * (Math.sin((207.4 + 1.5 * T) / 180.0 * Math.PI) + Math.sin((291.2 + 22.81 * T) / 180.0 * Math.PI)) +
			 0.0004 * (Math.sin((234.9 + 315.56 * T) / 180.0 * Math.PI) + Math.sin((157.3 + 299.3 * T) / 180.0 * Math.PI) + Math.sin((21.1 + 720.02 * T) / 180.0 * Math.PI)) +
			 0.0003 * (Math.sin((352.5 + 1079.97 * T) / 180.0 * Math.PI) + Math.sin((329.7 + 44.43 * T) / 180.0 * Math.PI));
	  
	if(result > 360.0){ result = result - 360.0 * Math.floor(result / 360.0); }
	else if ( result < 0.0) { result = result + 360.0 * Math.ceil(result / 360.0); }

	return result;
   }

  //distance up to the sun
  public static double getSunDistance(double T)
   {
    double q = (0.007256 - 0.0000002 * T) * Math.sin((267.54 + 359.991 * T) / 180.0 * Math.PI) +
			0.000091 * Math.sin((265.1 + 719.98 * T) / 180.0 * Math.PI) +
			0.00003 * Math.sin(90 / 180.0 * Math.PI) +
			0.000013 * Math.sin((27.8 + 4452.67 * T) / 180.0 * Math.PI) +
			0.000007 * (Math.sin((254 + 450.4 * T) / 180.0 * Math.PI) + Math.sin((156 + 329.6 * T) / 180.0 * Math.PI));

    return Math.pow(10.0, q);
   }

  //
  public static double getRightAscension(double elon, double e)
   {
    double alpha = Math.atan(Math.cos(e / 180 * Math.PI) * Math.tan(elon / 180 * Math.PI)) / Math.PI *180;
    if(elon >= 180.0 && elon < 360.0)
     {
      while(alpha < 180.0 || alpha >= 360.0)
       {
        if(alpha < 180.0) alpha += 180.0;
        else if(alpha >= 360.0) alpha -= 180.0;
       } 
     }
    else if(elon >= 0.0 && elon < 180.0)
     {
      while(alpha < 0.0 || alpha >= 180.0)
       {
        if(alpha < 0.0) alpha += 180.0;
        else if(alpha >= 180.0) alpha -= 180.0;
       } 
     }
    return alpha;
   }

 //
  public static double getDeclination(double elon, double e)
   {
    return Math.asin(Math.sin(e / 180 * Math.PI) * Math.sin(elon / 180 * Math.PI)) / Math.PI * 180;
   }

 //
  public static double getInclination(double T)
   {
    return 23.439291 -0.000130042 * T;
   }

  //
  public static double getSidereal(double T, double d, double ramda)
   {
    double phai = 325.4606+360.007700536 * T + 0.00000003879 * T * T + 360.0 * d + ramda;
    while(phai < -180.0 || phai > 180.0)
     {
      if(phai < -180.0) phai += 360.0;
      else if(phai > 180.0) phai -= 360.0;
     }

    return phai;
   }

  //2ooo年基準の時間
  public static double getTime(double Year, double Month, double Day, double Hour)
   {
    double y = Year - 2000.0;
    if(Month <= 2) { Month +=12; y -=1; }

   //2000
    double K = 365 * y + 30 * Month + Day - 33.5 + Math.floor(3 * (Month + 1.0) / 5.0) + Math.floor(y / 4.0);

    return (K + Hour / 24.0 + getDeltaT(Year) / 86400.0) / 365.25;
   }

  //
  public static double getDeltaT(double Y)
   {
    //
    double deltaT = 0.0, t = 0;

    if(Y < 1961.0 && Y >= 1941.0){ t = Y - 1950.0; deltaT =  29.07 + 0.407 * t - Math.pow(t, 2.0 / 233.0) + Math.pow(t, 3.0 / 2547.0); }
    else if(Y < 1986.0 && Y >= 1961.0){ t = Y - 1975.0; deltaT = 45.45 + 1.067 * t - Math.pow(t, 2.0 / 260.0) - Math.pow(t, 3.0 / 718.0); }
    else if(Y < 2005.0 && Y >= 1986.0){ t = Y - 2000.0; deltaT = 63.86 + 0.3345 * t - 0.060374 * Math.pow(t,2.0) + 0.0017275 * Math.pow(t,3.0) + 0.000651814 * Math.pow(t,4.0) + 0.00002373599 * Math.pow(t,5.0);}
    else { t = Y - 2000; deltaT = 62.92 + 0.32217 * t + 0.005589 * Math.pow(t,2) ;}

    return deltaT;
   }

  //
  public static double getSunDiameter(double r)
   {
    return 0.266994 / r;
   }

  //視差
  public static double getParallax(double r)
   {
    return 0.00244428 / r;
   }

  //
  public static double getSunriseAltitude(double S, double E, double R, double P)
   {
    return -S - E- R + P;
   }

  //
  public static double getTwilightAltitude(double E, double P)
   {
    return -E - P -7.3611;
   }

  //時角
  public static double getTimeAngle(double k, double delta, double lat)
   {
    delta = delta / 180.0 * Math.PI;
    lat = lat / 180.0 * Math.PI;

    return Math.abs(Math.acos((Math.sin(k / 180.0 * Math.PI) - Math.sin(delta) * Math.sin(lat)) / (Math.cos(delta) * Math.cos(lat))) * 180.0 / Math.PI);
   }

  //
  public static double getSunAltitude(double asc ,double dec, double lat, double phai )
   {
    double t = (phai - asc) / 180.0 * Math.PI;
    double latitude = lat / 180.0 * Math.PI;
    double declination = dec / 180.0 * Math.PI;
    double result = Math.asin(Math.sin(declination)* Math.sin(latitude) + Math.cos(declination) * Math.cos(latitude) * Math.cos(t)) / Math.PI * 180.0;

    double R = 0.0167 / Math.tan((result + 8.6 / (result + 4.4)) /180.0 * Math.PI);

    return result + R;
   }

  //
  public static double reviseAngle(double angle)
   {
    if (angle >= 360.0 | angle < 0.0) return angle - Math.floor(angle / 360.0) * 360.0; 

    return angle;
   }
 }
