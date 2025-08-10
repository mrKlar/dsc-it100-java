package com.github.kmbulebu.dsc.it100;

import com.github.kmbulebu.dsc.it100.commands.read.ReadCommand;
import com.github.kmbulebu.dsc.it100.commands.read.ZoneOpenCommand;
import com.github.kmbulebu.dsc.it100.commands.write.WriteCommand;

import rx.Observable;
import rx.functions.Action1;

public class ExampleApp {

	public static void main(String[] args) {
		
		// Configure for Envisalink. Read configuration from system properties with defaults.
		final String host = System.getProperty("dsc.host", "envisalink");
		final int port = Integer.parseInt(System.getProperty("dsc.port", "4025"));

		String password = System.getProperty("dsc.password", "user");
		final String encryptedPassword = System.getProperty("dsc.password.encrypted");

		if (encryptedPassword != null && !encryptedPassword.isEmpty()) {
			// If an encrypted password is provided, decrypt it.
			// This will only work on the Windows machine where it was encrypted by the same user.
			if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
				try {
					password = com.github.kmbulebu.dsc.it100.security.PasswordUtil.decrypt(encryptedPassword);
				} catch (Exception e) {
					System.err.println("FATAL: Failed to decrypt password. The service will likely fail to connect.");
					e.printStackTrace();
				}
			} else {
				System.err.println("WARNING: 'dsc.password.encrypted' is set, but this is not a Windows system. Decryption is not possible. Falling back to default password.");
			}
		}

		final IT100 it100 = new IT100(new ConfigurationBuilder().withRemoteSocket(host, port)
				.withEnvisalinkPassword(password).build());
		
		try {	
			// Start communicating with IT-100.
			it100.connect();
			
			final Observable<ReadCommand> readObservable = it100.getReadObservable();
			
			// Labels gives us friendly names to our zones.
			final Labels labels = new Labels(readObservable, it100.getWriteObservable());
			
			it100.getWriteObservable().subscribe(new Action1<WriteCommand>() {

				@Override
				public void call(WriteCommand command) {
					System.out.println("Write: " + System.currentTimeMillis() + " " + command.getCommandCode() + " " + command.getData());
				}
				
			});
			
			// Subscribe to all
			readObservable.subscribe(new Action1<ReadCommand>() {

				@Override
				public void call(ReadCommand command) {
					// TODO Auto-generated method stub
					System.out.println("Read: " + System.currentTimeMillis() + " " + command.getCommandCode() + " " + command.toString());
				}
				
			});
			
			// Subscribe to Zone opening events to print zone labels (Only on IT-100)
			readObservable.ofType(ZoneOpenCommand.class).subscribe(new Action1<ZoneOpenCommand>() {

				@Override
				public void call(ZoneOpenCommand t1) {
					// Print time and name of zone that opened.
					System.out.println(System.currentTimeMillis() + " " + labels.getZoneLabel(t1.getZone()) + " opened.");
				}
				
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
