package server;

import common.RegistrationInterface;
import common.User;
import common.UsersDB;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

public class RegistrationClass extends RemoteServer implements RegistrationInterface {

    private static final long serialVersionUID = 1L;
    private final int RMI_Port = 4567;
    private UsersDB users;
    private ServerNotificationService notificationService;

    public RegistrationClass(UsersDB u, ServerNotificationService ns) {
        users = u;
        notificationService = ns;
	}

	//pubblico il riferimento all'oggetto remoto
    public void start(){
        try {
            RegistrationInterface stub = (RegistrationInterface) UnicastRemoteObject.exportObject(this, 0);  //creo l'oggetto da esportare
            LocateRegistry.createRegistry(RMI_Port);            //creo un registry sulla porta RMI
            Registry r = LocateRegistry.getRegistry(RMI_Port);  //recupero il registry appena creato
            r.rebind("RegisterUser", stub);                     //pubblico il riferimento sotto il nome di RegisterUser
        } catch (RemoteException e) {
            e.printStackTrace();
        }   
    }


    @Override
    public synchronized String register(String[] myArgs) throws RemoteException {

        if(myArgs.length != 3)
            return "Error. Use registration username password";

        String nickname = myArgs[1];
        String password = myArgs[2];

        try{
            User u = users.getUser(nickname);           //controllo se l'utente e' gia' iscritto
            u.equals(null);                             //Se l'utente che sto cercando non esiste allora solleva l'eccezione perchè u è null

            return "Error. User "+nickname+" already registered";
        }catch(NullPointerException e){}

        
        synchronized(users){        //accedo in mutua esclusione alla struttura dati degli utenti

            users.addUser(new User(nickname, password));        //aggiorno la struttura dati

            // TODO: 08/01/2023 Questo aggiornamento non serve 
            //notificationService.update(users);                  //notifico l'aggiornamento

        }
        return "User "+nickname+" has been registered correctly. Login to continue.";
    }





}
