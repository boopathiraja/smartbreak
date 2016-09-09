package com.sdl.hellosdlandroid;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.smartdevicelink.exception.SdlException;
import com.smartdevicelink.exception.SdlExceptionCause;
import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.RPCRequestFactory;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.callbacks.OnServiceEnded;
import com.smartdevicelink.proxy.callbacks.OnServiceNACKed;
import com.smartdevicelink.proxy.interfaces.IProxyListenerALM;
import com.smartdevicelink.proxy.rpc.AddCommand;
import com.smartdevicelink.proxy.rpc.AddCommandResponse;
import com.smartdevicelink.proxy.rpc.AddSubMenuResponse;
import com.smartdevicelink.proxy.rpc.Alert;
import com.smartdevicelink.proxy.rpc.AlertManeuverResponse;
import com.smartdevicelink.proxy.rpc.AlertResponse;
import com.smartdevicelink.proxy.rpc.ChangeRegistrationResponse;
import com.smartdevicelink.proxy.rpc.Choice;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSet;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteCommandResponse;
import com.smartdevicelink.proxy.rpc.DeleteFile;
import com.smartdevicelink.proxy.rpc.DeleteFileResponse;
import com.smartdevicelink.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteSubMenuResponse;
import com.smartdevicelink.proxy.rpc.DiagnosticMessageResponse;
import com.smartdevicelink.proxy.rpc.DialNumberResponse;
import com.smartdevicelink.proxy.rpc.EndAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.GenericResponse;
import com.smartdevicelink.proxy.rpc.GetDTCsResponse;
import com.smartdevicelink.proxy.rpc.GetVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.Image;
import com.smartdevicelink.proxy.rpc.ListFiles;
import com.smartdevicelink.proxy.rpc.ListFilesResponse;
import com.smartdevicelink.proxy.rpc.MenuParams;
import com.smartdevicelink.proxy.rpc.OnAudioPassThru;
import com.smartdevicelink.proxy.rpc.OnButtonEvent;
import com.smartdevicelink.proxy.rpc.OnButtonPress;
import com.smartdevicelink.proxy.rpc.OnCommand;
import com.smartdevicelink.proxy.rpc.OnDriverDistraction;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.OnHashChange;
import com.smartdevicelink.proxy.rpc.OnKeyboardInput;
import com.smartdevicelink.proxy.rpc.OnLanguageChange;
import com.smartdevicelink.proxy.rpc.OnLockScreenStatus;
import com.smartdevicelink.proxy.rpc.OnPermissionsChange;
import com.smartdevicelink.proxy.rpc.OnStreamRPC;
import com.smartdevicelink.proxy.rpc.OnSystemRequest;
import com.smartdevicelink.proxy.rpc.OnTBTClientState;
import com.smartdevicelink.proxy.rpc.OnTouchEvent;
import com.smartdevicelink.proxy.rpc.OnVehicleData;
import com.smartdevicelink.proxy.rpc.PerformAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.PerformInteractionResponse;
import com.smartdevicelink.proxy.rpc.PutFile;
import com.smartdevicelink.proxy.rpc.PutFileResponse;
import com.smartdevicelink.proxy.rpc.ReadDIDResponse;
import com.smartdevicelink.proxy.rpc.ResetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.ScrollableMessageResponse;
import com.smartdevicelink.proxy.rpc.SendLocation;
import com.smartdevicelink.proxy.rpc.SendLocationResponse;
import com.smartdevicelink.proxy.rpc.SetAppIconResponse;
import com.smartdevicelink.proxy.rpc.SetDisplayLayout;
import com.smartdevicelink.proxy.rpc.SetDisplayLayoutResponse;
import com.smartdevicelink.proxy.rpc.SetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimerResponse;
import com.smartdevicelink.proxy.rpc.Show;
import com.smartdevicelink.proxy.rpc.ShowConstantTbtResponse;
import com.smartdevicelink.proxy.rpc.ShowResponse;
import com.smartdevicelink.proxy.rpc.SliderResponse;
import com.smartdevicelink.proxy.rpc.SoftButton;
import com.smartdevicelink.proxy.rpc.SpeakResponse;
import com.smartdevicelink.proxy.rpc.StreamRPCResponse;
import com.smartdevicelink.proxy.rpc.SubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SystemRequestResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.UpdateTurnListResponse;
import com.smartdevicelink.proxy.rpc.enums.FileType;
import com.smartdevicelink.proxy.rpc.enums.HMILevel;
import com.smartdevicelink.proxy.rpc.enums.ImageType;
import com.smartdevicelink.proxy.rpc.enums.LockScreenStatus;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;
import com.smartdevicelink.proxy.rpc.enums.SoftButtonType;
import com.smartdevicelink.proxy.rpc.enums.TextAlignment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Param;
import se.walkercrou.places.Place;

public class SdlService extends Service implements IProxyListenerALM{

    private static final String TAG 					= "Smart Breaks";

    private static final String APP_NAME 				= "Smart Breaks";
    private static final String APP_ID 					= "896613555";

    private static final String ICON_FILENAME 			= "ic_launcher1.png";
    private int iconCorrelationId;

    List<String> remoteFiles;

    private static final String WELCOME_SHOW 			= "Welcome to Smart Breaks";
    private static final String WELCOME_SPEAK 			= "Welcome to Hello Smart Breaks";

    private static final String TEST_COMMAND_NAME 		= "Show Route";
    private static final int TEST_COMMAND_ID 			= 1;

    private static final String TEST_COMMAND_NAME_2 		= "Test Command 2";
    private static final int TEST_COMMAND_ID_2			= 2;

    // Conenction management
    private static final int CONNECTION_TIMEOUT = 180 * 1000;
    private Handler mConnectionHandler = new Handler(Looper.getMainLooper());

    // variable used to increment correlation ID for every request sent to SYNC
    public int autoIncCorrId = 0;
    // variable to contain the current state of the service
    private static SdlService instance = null;

    // variable to create and call functions of the SyncProxy
    private SdlProxyALM proxy = null;

    private boolean firstNonHmiNone = true;
    private boolean isVehicleDataSubscribed = false;
    private int homeCorelId;
    private int welcomeCorId;
    Alert req;

    RPCRequest rpcMessage;

    private static final String key = "AIzaSyCDlnWNTIgz2QPW_lTAcOnXepWCkMt12pc";

    private static final ArrayList<Location> arr = new ArrayList<Location>();
    private Double mohave_county_lat = 35.940250;
    private Double mohave_county_lon = -114.653351;

    private Double yavapai_county_lat = 34.431871;
    private Double yavapai_county_lon = -113.236115;

    private Double maricopa_country_lat = 33.813423;
    private Double maricopa_county_lon = -112.522004;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        /**
         *
         * load values
         */

        Location loc = new Location(33.236430, -111.972687);
        Location loc1 = new Location(maricopa_country_lat, maricopa_county_lon);
        Location loc2 = new Location(yavapai_county_lat, yavapai_county_lon);
        Location loc3 = new Location(mohave_county_lat, mohave_county_lon);
        Location loc4 = new Location(33.941120, -112.686799);
        Location loc5 = new Location(34.141401, -112.994416);
        Location loc6 = new Location(34.883716, -113.598664);
        Location loc7 = new Location(35.135671, -113.642609);
        Location loc8 = new Location(35.162621, -113.851350);
        Location loc9 = new Location(36.011377, -114.763215);
        Location loc10 = new Location(36.020263, -114.774201);

        arr.add(loc);
        arr.add(loc1);
        arr.add(loc2);
        arr.add(loc3);
        arr.add(loc4);
        arr.add(loc5);
        arr.add(loc6);
        arr.add(loc7);
        arr.add(loc8);
        arr.add(loc9);
        arr.add(loc10);

        Log.d(TAG, "onCreate");
        super.onCreate();
        instance = this;
        remoteFiles = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startProxy();

        mConnectionHandler.postDelayed(mCheckConnectionRunnable, CONNECTION_TIMEOUT);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        disposeSyncProxy();
        instance = null;
        super.onDestroy();
    }

    public static SdlService getInstance() {
        return instance;
    }

    public SdlProxyALM getProxy() {
        return proxy;
    }

    public void startProxy() {

        Log.i(TAG, "Trying to start proxy");
        if (proxy == null) {
            try {
                Log.i(TAG, "Starting SDL Proxy");
                proxy = new SdlProxyALM(this, APP_NAME, true, APP_ID);
            } catch (SdlException e) {
                e.printStackTrace();
                // error creating proxy, returned proxy = null
                if (proxy == null) {
                    stopSelf();
                }
            }
        }
    }

    public void disposeSyncProxy() {
        LockScreenActivity.updateLockScreenStatus(LockScreenStatus.OFF);

        if (proxy != null) {
            try {
                proxy.dispose();
            } catch (SdlException e) {
                e.printStackTrace();
            }
            proxy = null;

        }
        this.firstNonHmiNone = true;
        this.isVehicleDataSubscribed = false;

    }

    public void reset() {
        if (proxy != null) {
            try {
                proxy.resetProxy();
                this.firstNonHmiNone = true;
                this.isVehicleDataSubscribed = false;
            } catch (SdlException e1) {
                e1.printStackTrace();
                //something goes wrong, & the proxy returns as null, stop the service.
                // do not want a running service with a null proxy
                if (proxy == null) {
                    stopSelf();
                }
            }
        } else {
            startProxy();
        }
    }

    /**
     * Will show a sample test message on screen as well as speak a sample test message
     */
    public void showTest(){
        try {
            proxy.show(TEST_COMMAND_NAME, "Command has been selected", TextAlignment.CENTERED, autoIncCorrId++);
            proxy.speak(TEST_COMMAND_NAME, autoIncCorrId++);
        } catch (SdlException e) {
            e.printStackTrace();
        }
    }




    /**
     *  Add commands for the app on SDL.
     */
    public void sendCommands(){
        AddCommand command = new AddCommand();
        MenuParams params = new MenuParams();
        params.setMenuName(TEST_COMMAND_NAME);
        command = new AddCommand();
        command.setCmdID(TEST_COMMAND_ID);
        command.setMenuParams(params);
        command.setVrCommands(Arrays.asList(new String[]{TEST_COMMAND_NAME}));
        sendRpcRequest(command);

        command = new AddCommand();
        params.setMenuName(TEST_COMMAND_NAME_2);
        command.setCmdID(TEST_COMMAND_ID_2);
        command.setMenuParams(params);
        command.setVrCommands(Arrays.asList(new String[]{TEST_COMMAND_NAME_2}));
        sendRpcRequest(command);
    }

    /**
     * Sends an RPC Request to the connected head unit. Automatically adds a correlation id.
     * @param request
     */
    private void sendRpcRequest(RPCRequest request){
        request.setCorrelationID(autoIncCorrId++);
        try {
            proxy.sendRPCRequest(request);
        } catch (SdlException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends an RPC Request to the connected head unit. Automatically adds a correlation id.
     */
    private void sendAlert(){
        try {
            // PerformInteraction();
            SoftButton softButton = new SoftButton();
            softButton.setText("Yes");
            softButton.setSoftButtonID(101);
            softButton.setType(SoftButtonType.SBT_TEXT);
            //softButton.setSystemAction(SystemAction.STEAL_FOCUS);
            SoftButton softButton1 = new SoftButton();
            softButton1.setText("NO");
            softButton1.setSoftButtonID(102);
            softButton1.setType(SoftButtonType.SBT_TEXT);
            Vector v = new Vector();
            v.add(softButton);
            v.add(softButton1);

           //Speak req1 = RPCRequestFactory.buildSpeak("Break Time Do you want to locate near by restaurants", autoIncCorrId++);
           // proxy.sendRPCRequest(req1);

            //req = RPCRequestFactory.buildAlert("Break Time..!!", "Do you want to locate near by restaurants?",10000,autoIncCorrId++);
            Alert req = RPCRequestFactory.buildAlert("Break Time..!!", "Do you want to locate near by restaurants", "?", 10000, v,autoIncCorrId++);
            proxy.sendRPCRequest(req);

            //proxy.alert("Break Time", true,autoIncCorrId++);
        } catch (SdlException e) {
            e.printStackTrace();
        }
    }

    private void PerformInteraction(){
        try {
            CreateInteractionChoiceSet createInteractionChoiceSet = new CreateInteractionChoiceSet();

            Vector<Choice> list = new Vector<>();
            Choice choice = new Choice();
            choice.setChoiceID(10);
            choice.setMenuName("YES");
            list.add(choice);
            Choice choice1 = new Choice();
            choice1.setChoiceID(11);
            choice1.setMenuName("NO");
            list.add(choice1);
            // createInteractionChoiceSet.setChoiceSet(list);
            CreateInteractionChoiceSet req1;
            req1 = RPCRequestFactory.buildCreateInteractionChoiceSet(list, 400, autoIncCorrId++);
            proxy.sendRPCRequest(req1);

            //proxy.alert("Break Time", true,autoIncCorrId++);
        } catch (SdlException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends the app icon through the uploadImage method with correct params
     * @throws SdlException
     */
    private void sendIcon() throws SdlException {
        Log.i(TAG, "send Icon: ");
        iconCorrelationId = autoIncCorrId++;
        // deleteImage(R.mipmap.ic_launcher1, ICON_FILENAME, iconCorrelationId, false);
        uploadImage(R.mipmap.ic_launcher, ICON_FILENAME, iconCorrelationId, true);

    }

    private void deleteImage(int resource, String imageName,int correlationId, boolean isPersistent){
        DeleteFile deleteFile=new DeleteFile();
        deleteFile.setSdlFileName(imageName);
        deleteFile.setCorrelationID(autoIncCorrId++);
        try {
            proxy.sendRPCRequest(deleteFile);
        } catch (SdlException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will help upload an image to the head unit
     * @param resource the R.drawable.__ value of the image you wish to send
     * @param imageName the filename that will be used to reference this image
     * @param correlationId the correlation id to be used with this request. Helpful for monitoring putfileresponses
     * @param isPersistent tell the system if the file should stay or be cleared out after connection.
     */
    private void uploadImage(int resource, String imageName,int correlationId, boolean isPersistent){
        PutFile putFile = new PutFile();
        putFile.setFileType(FileType.GRAPHIC_PNG);
        putFile.setSdlFileName(imageName);
        putFile.setCorrelationID(correlationId++);
        putFile.setPersistentFile(isPersistent);
        putFile.setSystemFile(false);
        putFile.setBulkData(contentsOfResource(resource));

        try {
            proxy.sendRPCRequest(putFile);
        } catch (SdlException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper method to take resource files and turn them into byte arrays
     * @param resource Resource file id.
     * @return Resulting byte array.
     */
    private byte[] contentsOfResource(int resource) {
        InputStream is = null;
        try {
            is = getResources().openRawResource(resource);
            ByteArrayOutputStream os = new ByteArrayOutputStream(is.available());
            final int bufferSize = 4096;
            final byte[] buffer = new byte[bufferSize];
            int available;
            while ((available = is.read(buffer)) >= 0) {
                os.write(buffer, 0, available);
            }
            return os.toByteArray();
        } catch (IOException e) {
            Log.w("SDL Service", "Can't read icon file", e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onProxyClosed(String info, Exception e, SdlDisconnectedReason reason) {

        if(!(e instanceof SdlException)){
            Log.v(TAG, "reset proxy in onproxy closed");
            reset();
        }
        else if ((((SdlException) e).getSdlExceptionCause() != SdlExceptionCause.SDL_PROXY_CYCLED))
        {
            if (((SdlException) e).getSdlExceptionCause() != SdlExceptionCause.BLUETOOTH_DISABLED)
            {
                Log.v(TAG, "reset proxy in onproxy closed");
                reset();
            }
        }


        stopSelf();
    }

    @Override
    public void onOnHMIStatus(OnHMIStatus notification) {
        Log.i(TAG, "onOnHMIStatus calling "+notification.getHmiLevel());
        if(notification.getHmiLevel().equals(HMILevel.HMI_FULL)){
            if (notification.getFirstRun()) {
                // send welcome message if applicable
                //uploadImage(R.drawable.starbucks_logo, "starbucks_logo.png", autoIncCorrId++, true);
                //uploadImage(R.drawable.chipotle_logo, "chipotle_logo.png", autoIncCorrId++, true);
                //uploadImage(R.drawable.mcdonalds, "mcdonalds.png", autoIncCorrId++, true);
                //uploadImage(R.drawable.safe_place, "safe_place.png", autoIncCorrId++, true);
                //uploadImage(R.drawable.smartbreakslarge, "smartbreakslarge.png", autoIncCorrId++, true);
                uploadImage(R.drawable.redcaution, "redcaution.png", autoIncCorrId++, true);
                uploadImage(R.drawable.dunkin_donuts, "dunkin_donuts.png", autoIncCorrId++, true);



                performWelcomeMessage();



               // sendAlert();
                /*Log.i(TAG, "getvehicledata calling ");
                try {
                    proxy.getvehicledata(true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,autoIncCorrId++);
                } catch (SdlException e) {
                    e.printStackTrace();
                }*/
            }
            // Other HMI (Show, PerformInteraction, etc.) would go here
        }


        if(!notification.getHmiLevel().equals(HMILevel.HMI_NONE)
                && firstNonHmiNone){
            //sendCommands();
            //uploadImages();
            firstNonHmiNone = false;

            // Other app setup (SubMenu, CreateChoiceSet, etc.) would go here
        }else{
            //We have HMI_NONE
            if(notification.getFirstRun()){
                uploadImages();
                mConnectionHandler.removeCallbacksAndMessages(null);
            }
        }

    }

    /**
     * Will show a sample welcome message on screen as well as speak a sample welcome message
     */
    private void performWelcomeMessage(){
        try {
            //Set the welcome message on screen
            // proxy.show(APP_NAME, WELCOME_SHOW, TextAlignment.CENTERED, autoIncCorrId++);
            SetDisplayLayout layout = new SetDisplayLayout();
            layout.setDisplayLayout("TEXT_WITH_GRAPHIC");
            homeCorelId = autoIncCorrId++;
            layout.setCorrelationID(homeCorelId);

            try {
                proxy.sendRPCRequest(layout);
            } catch (Exception e) {
                e.printStackTrace();
            }



            //Say the welcome message
            welcomeCorId = autoIncCorrId++;
            proxy.speak(WELCOME_SPEAK, welcomeCorId);



        } catch (SdlException e) {
           e.printStackTrace();
       }

    }

    /**
     * Will show a sample welcome message on screen as well as speak a sample welcome message
     */
    private void performWelcomeMessage1(){
        try {
            //Set the welcome message on screen
            proxy.show(APP_NAME, "Hello clicked from Alert", TextAlignment.CENTERED, autoIncCorrId++);

            //Say the welcome message
            proxy.speak(WELCOME_SPEAK, autoIncCorrId++);

        } catch (SdlException e) {
            e.printStackTrace();
        }

    }

    /**
     *  Requests list of images to SDL, and uploads images that are missing.
     */
    private void uploadImages(){
        ListFiles listFiles = new ListFiles();
        this.sendRpcRequest(listFiles);

    }

    @Override
    public void onListFilesResponse(ListFilesResponse response) {
        Log.i(TAG, "onListFilesResponse from SDL ");
        if(response.getSuccess()){
            remoteFiles = response.getFilenames();
        }

        // Check the mutable set for the AppIcon
        // If not present, upload the image
        if(true || remoteFiles== null || !remoteFiles.contains(SdlService.ICON_FILENAME)){
            try {
                sendIcon();
            } catch (SdlException e) {
                e.printStackTrace();
            }
        }else{
            // If the file is already present, send the SetAppIcon request
            try {
                proxy.setappicon(ICON_FILENAME, autoIncCorrId++);
            } catch (SdlException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPutFileResponse(PutFileResponse response) {
        Log.i(TAG, "onPutFileResponse from SDL :" +response.getCorrelationID());
        Log.i(TAG, "response.getCorrelationID(): "+response.getCorrelationID() + "-"+iconCorrelationId);
        if(response.getCorrelationID() == iconCorrelationId){ //If we have successfully uploaded our icon, we want to set it
            try {
                proxy.setappicon(ICON_FILENAME, autoIncCorrId++);
            } catch (SdlException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onOnLockScreenNotification(OnLockScreenStatus notification) {
        LockScreenActivity.updateLockScreenStatus(notification.getShowLockScreen());
    }

    @Override
    public void onOnCommand(OnCommand notification){
        Integer id = notification.getCmdID();
        if(id != null){
            switch(id){
                case TEST_COMMAND_ID:
                    showTest();
                    break;
                case TEST_COMMAND_ID_2:
                    // showTest1();
                    break;
            }
            //onAddCommandClicked(id);
        }
    }

    /**
     *  Callback method that runs when the add command response is received from SDL.
     */
    @Override
    public void onAddCommandResponse(AddCommandResponse response) {
        Log.i(TAG, "AddCommand response from SDL: " + response.getResultCode().name());

    }


	/*  Vehicle Data   */


    @Override
    public void onOnPermissionsChange(OnPermissionsChange notification) {
        Log.i(TAG, "Permision changed: " + notification);
		/* Uncomment to subscribe to vehicle data
		List<PermissionItem> permissions = notification.getPermissionItem();
		for(PermissionItem permission:permissions){
			if(permission.getRpcName().equalsIgnoreCase(FunctionID.SUBSCRIBE_VEHICLE_DATA.name())){
				if(permission.getHMIPermissions().getAllowed()!=null && permission.getHMIPermissions().getAllowed().size()>0){
					if(!isVehicleDataSubscribed){ //If we haven't already subscribed we will subscribe now
						//TODO: Add the vehicle data items you want to subscribe to
						//proxy.subscribevehicledata(gps, speed, rpm, fuelLevel, fuelLevel_State, instantFuelConsumption, externalTemperature, prndl, tirePressure, odometer, beltStatus, bodyInformation, deviceStatus, driverBraking, correlationID);
						proxy.subscribevehicledata(false, true, rpm, false, false, false, false, false, false, false, false, false, false, false, autoIncCorrId++);
					}
				}
			}
		}
		*/
    }

    @Override
    public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse response) {
        if(response.getSuccess()){
            Log.i(TAG, "Subscribed to vehicle data");
            this.isVehicleDataSubscribed = true;
        }
    }

    @Override
    public void onOnVehicleData(OnVehicleData notification) {
        Log.i(TAG, "Vehicle data notification from SDL");
        //TODO Put your vehicle data code here
        //ie, notification.getSpeed().

    }

    /**
     * Rest of the SDL callbacks from the head unit
     */

    @Override
    public void onAddSubMenuResponse(AddSubMenuResponse response) {
        Log.i(TAG, "AddSubMenu response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
    }

    @Override
    public void onCreateInteractionChoiceSetResponse(CreateInteractionChoiceSetResponse response) {
        Log.i(TAG, "CreateInteractionChoiceSet response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onAlertResponse(AlertResponse response) {
        Log.i(TAG, "Alert response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
        Log.i(TAG, "Alert response from SDL 1: " + response.getFunctionName());
        Log.i(TAG, "Alert response from SDL 2: " + response.getMessageType());
        Log.i(TAG, "Alert response from SDL 2: " + response.getParameters("Alert"));
        Log.i(TAG, "Alert response from SDL 2: " + response.getSuccess());
    }

    @Override
    public void onDeleteCommandResponse(DeleteCommandResponse response) {
        Log.i(TAG, "DeleteCommand response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
    }

    @Override
    public void onDeleteInteractionChoiceSetResponse(DeleteInteractionChoiceSetResponse response) {
        Log.i(TAG, "DeleteInteractionChoiceSet response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
    }

    @Override
    public void onDeleteSubMenuResponse(DeleteSubMenuResponse response) {
        Log.i(TAG, "DeleteSubMenu response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
    }

    @Override
    public void onPerformInteractionResponse(PerformInteractionResponse response) {
        Log.i(TAG, "PerformInteraction response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
    }

    @Override
    public void onResetGlobalPropertiesResponse(
            ResetGlobalPropertiesResponse response) {
        Log.i(TAG, "ResetGlobalProperties response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
    }

    @Override
    public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse response) {
        Log.i(TAG, "SetGlobalProperties response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
    }

    @Override
    public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse response) {
        Log.i(TAG, "SetMediaClockTimer response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
    }

    @Override
    public void onShowResponse(ShowResponse response) {
        Log.i(TAG, "Show response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
      //  sendAlert();
    }

    @Override
    public void onSpeakResponse(SpeakResponse response) {
        Log.i(TAG, "SpeakCommand response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

            //if(response.getCorrelationID() == welcomeCorId) { //If we have successfully uploaded our icon, we want to set it

                sendAlert();
            //}
    }

    @Override
    public void onOnButtonEvent(OnButtonEvent notification) {
        Log.i(TAG, "OnButtonEvent notification from SDL: " + notification);
    }

    public void sendNavigation(Double lat, Double lang) {
        Log.i(TAG, "sendNavigation calling: ");


        List<BreaksLoc> locList = getLocation();

        Log.i(TAG, "getLatitude: " + lat);
        Log.i(TAG, "getLongitude: " + lang);

        SendLocation sendLocation = new SendLocation();
        sendLocation.setLatitudeDegrees(lat);
        sendLocation.setLongitudeDegrees(lang);
        sendLocation.setLocationDescription("coffee shop");
        sendLocation.setLocationName("Dunkin Donuts tatum");
        sendLocation.setCorrelationID(autoIncCorrId++);

        try {
            proxy.sendRPCRequest(sendLocation);
        } catch (Exception e) {
            Log.i(TAG, "Exception" + e.toString());
        }
    }



    @Override
    public void onOnButtonPress(OnButtonPress notification) {
        Log.i(TAG, "OnButtonPress notification from SDL: " + notification);
        Log.i(TAG, "OnButtonPress notification from SDL1: " + notification.getCustomButtonName().toString());




        if(notification.getCustomButtonName() != null) {
            if("101".equalsIgnoreCase(notification.getCustomButtonName().toString())){
                SetDisplayLayout layout = new SetDisplayLayout();
                layout.setDisplayLayout("TILES_WITH_GRAPHIC");
                layout.setCorrelationID(autoIncCorrId++);

                try{
                     //RPCRequestFactory.build
                    proxy.sendRPCRequest(layout);
                } catch (Exception e) {

                }
            }else if("104".equalsIgnoreCase(notification.getCustomButtonName().toString())){
                SetDisplayLayout layout = new SetDisplayLayout();
                layout.setDisplayLayout("TILES_WITH_GRAPHIC");
                layout.setCorrelationID(autoIncCorrId++);

                try{
                    //List<BreaksLoc> locList =  getLocation();
                    //sendNavigation(Double.valueOf(locList.get(0).getLatitude()),Double.valueOf(locList.get(0).getLongitude()));
                    sendNavigation(maricopa_country_lat, maricopa_county_lon);


                    //RPCRequestFactory.build
                    //proxy.sendRPCRequest(layout);
                } catch (Exception e) {

                }
            }else if("106".equalsIgnoreCase(notification.getCustomButtonName().toString())){
                SetDisplayLayout layout = new SetDisplayLayout();
                layout.setDisplayLayout("TILES_WITH_GRAPHIC");
                layout.setCorrelationID(autoIncCorrId++);

                try{
                    //RPCRequestFactory.build
                    proxy.sendRPCRequest(layout);
                } catch (Exception e) {

                }
            }else{
                SetDisplayLayout layout = new SetDisplayLayout();
                layout.setDisplayLayout("TILES_WITH_GRAPHIC");
                layout.setCorrelationID(autoIncCorrId++);

                try{
                    //RPCRequestFactory.build
                    proxy.sendRPCRequest(layout);
                } catch (Exception e) {

                }
            }
        }
    }

    @Override
    public void onSubscribeButtonResponse(SubscribeButtonResponse response) {
        Log.i(TAG, "SubscribeButton response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
    }

    @Override
    public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse response) {
        Log.i(TAG, "UnsubscribeButton response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
    }


    @Override
    public void onOnTBTClientState(OnTBTClientState notification) {
        Log.i(TAG, "OnTBTClientState notification from SDL: " + notification);
    }

    @Override
    public void onUnsubscribeVehicleDataResponse(
            UnsubscribeVehicleDataResponse response) {
        Log.i(TAG, "UnsubscribeVehicleData response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onGetVehicleDataResponse(GetVehicleDataResponse response) {
        Log.i(TAG, "GetVehicleData response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
        /*Log.i(TAG, "Longitude " + response.getGps().getLongitudeDegrees().toString());
        Log.i(TAG, "Latitude" + response.getGps().getLatitudeDegrees().toString());
        Log.i(TAG, "Fuel Level" + String.valueOf(response.getFuelLevel().intValue()));
        Log.i(TAG, "FuelLevel State " + response.getFuelLevelState().toString());
        Log.i(TAG, "actual gps " + String.valueOf(response.getGps().getActual().booleanValue()));
        Log.i(TAG, String.valueOf(response.getGps().getHdop().intValue()));
        Log.i(TAG, String.valueOf(response.getGps().getUtcDay()));
        Log.i(TAG, String.valueOf(response.getGps().getUtcHours()));
        Log.i(TAG, String.valueOf(response.getGps().getUtcMonth()));
        Log.i(TAG, String.valueOf(response.getGps().getUtcMinutes()));
        Log.i(TAG, String.valueOf(response.getGps().getUtcSeconds()));
        Log.i(TAG, String.valueOf(response.getGps().getUtcYear()));


        SendLocation sendLocation = new SendLocation();
        sendLocation.setLatitudeDegrees(response.getGps().getLatitudeDegrees());
        sendLocation.setLongitudeDegrees(response.getGps().getLongitudeDegrees());
        sendLocation.setLocationDescription("coffee shop");
        sendLocation.setLocationName("starbucks tatum");
        sendLocation.setCorrelationID(autoIncCorrId++);

        try {
            proxy.sendRPCRequest(sendLocation);
        } catch (Exception e) {
            Log.i(TAG, "Exception" + e.toString());
        }*/

    }

    @Override
    public void onReadDIDResponse(ReadDIDResponse response) {
        Log.i(TAG, "ReadDID response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onGetDTCsResponse(GetDTCsResponse response) {
        Log.i(TAG, "GetDTCs response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }


    @Override
    public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response) {
        Log.i(TAG, "PerformAudioPassThru response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onEndAudioPassThruResponse(EndAudioPassThruResponse response) {
        Log.i(TAG, "EndAudioPassThru response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onOnAudioPassThru(OnAudioPassThru notification) {
        Log.i(TAG, "OnAudioPassThru notification from SDL: " + notification );

    }

    @Override
    public void onDeleteFileResponse(DeleteFileResponse response) {
        Log.i(TAG, "DeleteFile response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onSetAppIconResponse(SetAppIconResponse response) {
        Log.i(TAG, "SetAppIcon response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onScrollableMessageResponse(ScrollableMessageResponse response) {
        Log.i(TAG, "ScrollableMessage response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onChangeRegistrationResponse(ChangeRegistrationResponse response) {
        Log.i(TAG, "ChangeRegistration response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse response) {
        Log.i(TAG, "correlaation id: "+ response.getCorrelationID() +"--"+homeCorelId);

        if(response.getCorrelationID() == homeCorelId){ //If we have successfully uploaded our icon, we want to set it
            try {
                Show show = new Show();
                Image image3 = new Image();
                image3.setValue("smartbreakslarge.png");
                image3.setImageType(ImageType.DYNAMIC);
                show.setMainField1("Your next alert is approx after");
                //show.setStatusBar("Your next alert is approx after 30 mins.");
                show.setMainField2("30 mins ");
                show.setMainField3("Smart prediction in progress!!!");
                //show.setMainField4("hij");
                show.setGraphic(image3);
                show.setCorrelationID(autoIncCorrId++);
                proxy.sendRPCRequest(show);
            } catch (SdlException e) {
                e.printStackTrace();
            }
        }else {


            Log.i(TAG, "SetDisplayLayout response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

            //Hashtable hashtable = new Hashtable();
            // hashtable.put("Name","Star");

            List list = new ArrayList();

            SoftButton softButton = new SoftButton();
            softButton.setText("4.5 Stars");
            softButton.setSoftButtonID(104);
            softButton.setType(SoftButtonType.SBT_BOTH);

            Image image = new Image();
            image.setValue("dunkin_donuts.png");
            image.setImageType(ImageType.DYNAMIC);
            // image.set

            softButton.setImage(image);

            //softButton.setSystemAction(SystemAction.STEAL_FOCUS);
            SoftButton softButton1 = new SoftButton();
            softButton1.setText("4.5 Stars");
            softButton1.setSoftButtonID(105);
            softButton1.setType(SoftButtonType.SBT_BOTH);

            Image image1 = new Image();
            image1.setValue("chipotle_logo.png");
            image1.setImageType(ImageType.DYNAMIC);
            softButton1.setImage(image1);

            SoftButton softButton2 = new SoftButton();
            softButton2.setText("3.5 Stars");
            softButton2.setSoftButtonID(106);
            softButton2.setType(SoftButtonType.SBT_BOTH);

            Image image2 = new Image();
            image2.setValue("mcdonalds.png");
            image2.setImageType(ImageType.DYNAMIC);
            softButton2.setImage(image2);

            Vector v = new Vector();
            v.add(softButton);
            v.add(softButton1);
            v.add(softButton2);


            Show show = new Show();
            show.setSoftButtons(v);
            show.setCorrelationID(autoIncCorrId++);

            Image image3 = new Image();
            //image3.setValue("safe_place.png");
            image3.setValue("redcaution.png");

            image3.setImageType(ImageType.DYNAMIC);

            show.setGraphic(image3);
            try {
                proxy.sendRPCRequest(show);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onOnLanguageChange(OnLanguageChange notification) {
        Log.i(TAG, "OnLanguageChange notification from SDL: " + notification);

    }

    @Override
    public void onSliderResponse(SliderResponse response) {
        Log.i(TAG, "Slider response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }


    @Override
    public void onOnHashChange(OnHashChange notification) {
        Log.i(TAG, "OnHashChange notification from SDL: " + notification);

    }

    @Override
    public void onOnSystemRequest(OnSystemRequest notification) {
        Log.i(TAG, "OnSystemRequest notification from SDL: " + notification);

    }

    @Override
    public void onSystemRequestResponse(SystemRequestResponse response) {
        Log.i(TAG, "SystemRequest response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onOnKeyboardInput(OnKeyboardInput notification) {
        Log.i(TAG, "OnKeyboardInput notification from SDL: " + notification);

    }

    @Override
    public void onOnTouchEvent(OnTouchEvent notification) {
        Log.i(TAG, "OnTouchEvent notification from SDL: " + notification);

    }

    @Override
    public void onDiagnosticMessageResponse(DiagnosticMessageResponse response) {
        Log.i(TAG, "DiagnosticMessage response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onOnStreamRPC(OnStreamRPC notification) {
        Log.i(TAG, "OnStreamRPC notification from SDL: " + notification);

    }

    @Override
    public void onStreamRPCResponse(StreamRPCResponse response) {
        Log.i(TAG, "StreamRPC response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onDialNumberResponse(DialNumberResponse response) {
        Log.i(TAG, "DialNumber response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onSendLocationResponse(SendLocationResponse response) {
        Log.i(TAG, "SendLocation response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onServiceEnded(OnServiceEnded serviceEnded) {

    }

    @Override
    public void onServiceNACKed(OnServiceNACKed serviceNACKed) {

    }

    @Override
    public void onShowConstantTbtResponse(ShowConstantTbtResponse response) {
        Log.i(TAG, "ShowConstantTbt response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onAlertManeuverResponse(AlertManeuverResponse response) {
        Log.i(TAG, "AlertManeuver response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onUpdateTurnListResponse(UpdateTurnListResponse response) {
        Log.i(TAG, "UpdateTurnList response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

    }

    @Override
    public void onServiceDataACK() {

    }

    @Override
    public void onOnDriverDistraction(OnDriverDistraction notification) {
        // Some RPCs (depending on region) cannot be sent when driver distraction is active.
    }

    @Override
    public void onError(String info, Exception e) {
    }

    @Override
    public void onGenericResponse(GenericResponse response) {
        Log.i(TAG, "Generic response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
    }

    private Runnable mCheckConnectionRunnable = new Runnable() {
        @Override
        public void run() {
            stopSelf();
        }
    };




    public List<BreaksLoc> getLocation() {

        Random r = new Random();
        int Low = 0;
        int High = 10;
        int i = r.nextInt(High - Low) + Low;

        List<BreaksLoc> data = null;
        try {
            Location l = arr.get(i);
            if (i % 2 == 0) {
                data = getCoffeeShops(l.loc_lat, l.loc_lon);
            } else {
                data = getRestaurants(l.loc_lat, l.loc_lon);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG,"my Data \t"+data.toString());
        return  data;
    }

    public List<BreaksLoc> getCoffeeShops(double lat, double lon) {

        GooglePlaces client = new GooglePlaces(key);
        List<BreaksLoc> loc = new ArrayList<>();

       /*
       Specific to particular lat long
        */
        List<Place> sbplaces = client.getNearbyPlacesRankedByDistance(lat, lon, 3, Param.name("keyword").value("cafe"));
        Log.i(TAG, " Specific to particular lat long");
        for (Place p : sbplaces) {
            BreaksLoc b = new BreaksLoc();
            Log.i(TAG, p.getName());
            Log.i(TAG, String.valueOf(p.getLatitude()));
            Log.i(TAG, String.valueOf(p.getLongitude()));
            Log.i(TAG, String.valueOf(p.getRating()));

            String status = getCrimeData(String.valueOf(p.getLatitude()), String.valueOf(p.getLongitude()));
            b.setName(p.getName());
            b.setLatitude(String.valueOf(p.getLatitude()));
            b.setLongitude(String.valueOf(p.getLongitude()));
            b.setRating(String.valueOf(p.getRating()));
            b.setCrimeStatus(status);
            loc.add(b);
            Log.i(TAG, "Status \t" + status);

        }

        return loc;
    }



    private String getCrimeData(String lat, String lon) {
        String crimeStatus = "Green";

        try {
            URL url = new URL("http://api.spotcrime.com/crimes.json?lat=" + lat + "&lon=" + lon + "&radius=0.03&key=.&_=1473329650463");
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            //connection.addRequestProperty("x-api-key",
            //        context.getString(R.string.open_weather_maps_app_id));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp = "";
            while ((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());
            JSONArray jsonMainArr = data.getJSONArray("crimes");
            int sevenDayCount = 0;
            int thirtyDayCount = 0;
            for (int n = 0; n < jsonMainArr.length(); n++) {
                JSONObject object = jsonMainArr.getJSONObject(n);
                String dateStr = object.getString("date");
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
                Date crimeDate = sdf.parse(dateStr);

                Log.i("abc", crimeDate.toString());

                Date now = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.add(Calendar.DATE, -30);
                Date dateBefore30Days = cal.getTime();

                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(now);
                cal1.add(Calendar.DATE, -7);
                Date dateBefore7Days = cal1.getTime();

                if (crimeDate.compareTo(dateBefore30Days) > 0) {
                    thirtyDayCount++;
                }

                if (crimeDate.compareTo(dateBefore7Days) > 0) {
                    sevenDayCount++;
                }

            }

            if (sevenDayCount > 0) {
                crimeStatus = "Red";
            } else if (thirtyDayCount > 0) {
                crimeStatus = "Yellow";
            }

            // This value will be 404 if the request was not
            // successful
            // if(data.getInt("cod") != 200){
            ///    return null;
            // }

            //return data;
        } catch (Exception e) {
            return null;
        }

        Log.i(TAG, crimeStatus);
        return crimeStatus;
    }

    public List<BreaksLoc> getRestaurants(double lat, double lon) {

        GooglePlaces client = new GooglePlaces(key);
        List<BreaksLoc> loc = new ArrayList<>();

        List<Place> restaurants = client.getNearbyPlacesRankedByDistance(lat, lon, 3, Param.name("keyword").value("restaurant"));

        Log.i(TAG, "Restaurant particular to a lat long\n");
        for (Place p : restaurants) {
            BreaksLoc b = new BreaksLoc();
            Log.i(TAG, p.getName());
            Log.i(TAG, String.valueOf(p.getLatitude()));
            Log.i(TAG, String.valueOf(p.getLongitude()));
            Log.i(TAG, String.valueOf(p.getRating()));
            String status = getCrimeData(String.valueOf(p.getLatitude()), String.valueOf(p.getLongitude()));

            b.setName(p.getName());
            b.setLatitude(String.valueOf(p.getLatitude()));
            b.setLongitude(String.valueOf(p.getLongitude()));
            b.setRating(String.valueOf(p.getRating()));
            b.setCrimeStatus(status);
            loc.add(b);

        }
        return loc;
    }



    static class BreaksLoc {
        private String latitude;
        private String longitude;
        private String name;
        private String rating;
        private String crimeStatus;

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRating() {
            return rating;
        }

        public void setRating(String rating) {
            this.rating = rating;
        }

        public String getCrimeStatus() {
            return crimeStatus;
        }

        public void setCrimeStatus(String crimeStatus) {
            this.crimeStatus = crimeStatus;
        }
    }


    static class Location {



        private Double loc_lat;
        private Double loc_lon;

        public Location(Double loc_lat, Double loc_lon) {
            this.loc_lat = loc_lat;
            this.loc_lon = loc_lon;
        }
    }
}