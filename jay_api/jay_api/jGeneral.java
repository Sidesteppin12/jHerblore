package scripts.jay_api;

import java.util.List;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.util.abc.ABCUtil;
import org.tribot.api2007.ChooseOption;
import org.tribot.api2007.Game;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Magic;
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSItem;

import scripts.jay_api.fluffeespaint.Variables;

public class jGeneral {
	
    private jGeneral() {}
    private static final jGeneral GENERAL = new jGeneral();
    public static jGeneral get() {
        return GENERAL;
    }
	
	private int Count = 0;
	
	private ABCUtil util = new ABCUtil();
	
	public ABCUtil getUtil() {
		return util;
	}
	
	public void performABC() {
		if (util.shouldCheckTabs())
			util.checkTabs();

		if (util.shouldCheckXP())	
			util.checkXP();

		if (util.shouldExamineEntity())
			util.examineEntity();

		if (util.shouldMoveMouse())
			util.moveMouse();

		if (util.shouldPickupMouse())
			util.pickupMouse();

		if (util.shouldRightClick())
			util.rightClick();

		if (util.shouldRotateCamera())
			util.rotateCamera();

		if (util.shouldLeaveGame())
			util.leaveGame();
	}
	
	public int getCount() {
		return Count;
	}
	
	public void setCount(int val) {
		Count = val;	
	}
	
	public void occurenceCounter() {
		Count++;
	}
	
	// If true, checks the amount of cash in the bank. If false, checks the amount of cash in the inventory.
	public int getCash(boolean place) {
		
		RSItem item = null;
		if (place) {
			if (Banker.openBank())
				item = RS.Banking_find(995);
		}
		else
			item = RS.Inventory_find(995);
		
		if (item != null && item.getStack() != 0)
			return item.getStack();

		return 0;
	}
	
	public boolean waitInventory(int val) {
		if (Timing.waitCondition(() -> {
			General.sleep(300, 600);
			return Inventory.getAll().length != val;
		}, General.random(3000, 5000))) {
			return true; // We already slept after depositing so lets not do it again.
		}

		return false;
	}
	
	public boolean waitInventory_reverse(int val) {
		if (Timing.waitCondition(() -> {
			General.sleep(300, 600);
			return Inventory.getAll().length == val;
		}, General.random(3000, 5000))) {
			return true; // We already slept after depositing so lets not do it again.
		}

		return false;
	}

	public boolean waitInventory() {
		if (Timing.waitCondition(() -> {
			General.sleep(300, 600);
			return Inventory.getAll().length == 0;
		}, General.random(3000, 5000))) {
			return true; // We already slept after depositing so lets not do it again.
		}

		return false;
	}
	
	public boolean deselect() {

		if (Game.getItemSelectionState() != 0 || Magic.isSpellSelected()) {
		    if (Timing.waitCondition(() -> {
				util.moveMouse();
				General.sleep(200, 400);
				Mouse.click(3);
				General.sleep(200, 700);
				ChooseOption.select("Cancel");
				General.sleep(200, 700);
		        return Game.getItemSelectionState() == 0 && !Magic.isSpellSelected();
		    }, General.random(7000, 10000)) == false) {
		    	General.println("AutoGeneral_Error - Could not deselect mouse.");
		    	return false;
		    }
		}

		return true;
	}

	public boolean clickAll(int delay_min, int delay_max, boolean track) {
		List<RSItem> items = Inventory.findList(handlerXML.get().getWithdrawingItems().get(0));
		if (items != null && deselect()) {
			RSItem item = null;
			int ClickCounter = items.size(), rand = General.random(84, 140);
			RSItem[] tempArray = new RSItem[items.size()]; items.toArray(tempArray);
			for (int i = 0; i < tempArray.length; i++) {
				item = tempArray[i];
				
				// Skip a herb or two once in a while.
				if (tempArray.length > i+2 && General.random(0, rand) == 0) {
					i = i + General.random(1, 2);
					item = tempArray[i];
				}
				
				if (item == null || !item.click()) {
					General.println("AutoGeneral_Error - Could not click the desired item.");
					return false;
				}
				else if (track) {
					Variables.get().addToCreated(1);
				}
				
				General.sleep(delay_min, delay_max);
				ClickCounter--;
				items.set(i, null);
			}
			
			while (items.remove(null)) {
				continue;
			}

			if (ClickCounter != 0) {
				shortDynamicSleep();
				
				for (RSItem item_3 : items) {
					
					if (item_3 == null || !item_3.click()) {
						General.println("AutoGeneral_Error - Could not click the desired item.");
						return false;
					}
					else if (track) {
						Variables.get().addToCreated(1);
					}
					
					General.sleep(delay_min, delay_max);
				}
			}
			
			return true;
		}
		
		General.println("AutoGeneral_Error - Could not find the item.");
		return false;
	}
	
	public boolean clickMix(int itemID, int itemID_2, int finishedItem, boolean track, boolean ignoreLvl) {
		RSItem item = RS.Inventory_find(itemID);
		RSItem item_2 = RS.Inventory_find(itemID_2);
		
		RSItem[] array  = Inventory.find(itemID);
		if (array.length > 2 && General.random(0, 9) == 0) {
			item = array[General.random(1, 2)];
		}
		
		RSItem[] array2  = Inventory.find(itemID_2);
		if (array2.length > 2 && General.random(0, 9) == 0) {
			item_2 = array2[General.random(1, 2)];
		}
		
		if (item != null && item_2 != null && deselect()) {
			if (attemptMix(item, item_2)) {
				
				if (Inventory.getCount(itemID) != 1 && Inventory.getCount(itemID_2) != 1) {
					if (!Timing.waitCondition(() -> {
						General.sleep(200, 400);
						return Interfaces.get(270, 14) != null;
					}, 5000)) {
						General.println("AutoGeneral_Error - Could not find the box.");
						return false;
					}
					
					if (!Timing.waitCondition(() -> {
						Interfaces.get(270, 14).click();
						General.sleep(1000);
						return Interfaces.get(270, 14) == null;
					}, 5000)) {
						General.println("AutoGeneral_Error - Could not click the box.");
						return false;
					}
				}

				if (!Timing.waitCondition(() -> {
					//performABC(); // For some reason the abc doesnt work and just breaks the script.
					General.sleep(100);
					if (!ignoreLvl && handleLevelUp() && 
					   (Inventory.find(itemID).length != 0 && Inventory.find(itemID_2).length != 0)) {
						if (clickMix(itemID, itemID_2, finishedItem, track, ignoreLvl))
							return true;
						
						return false;
					}
					
					return Inventory.find(itemID).length == 0 || Inventory.find(itemID_2).length == 0 ;
				}, 20000)) {
					General.println("AutoGeneral_Error - Couldn't create all the potions.");
					return false;
				}
					
				if (track)
					Variables.get().addToCreated(Inventory.getCount(finishedItem));
					
				defaultDynamicSleep();
				return true;
			}

			return false;
		}
		
		General.println("AutoGeneral_Error - Could not find the item.");
		return false;
	}

	public boolean attemptMix(RSItem item, RSItem item_2) {
		if (!Timing.waitCondition(() -> {			
			item.click();
			General.sleep(200, 300);
			return Game.getItemSelectionState() == 1;
		}, General.random(2000, 3000))) {
			General.println("AutoGeneral_Error - Could not click the desired item.");
			return false;
		}
		
		shortDynamicSleep();
		
		if (!Timing.waitCondition(() -> {
			item_2.click();
			General.sleep(200, 300);
			return Game.getItemSelectionState() == 0;
		}, General.random(2000, 3000))) {
			General.println("AutoGeneral_Error - Could not click the desired item.");
			return false;
		}
		
		return true;
	}
	
	public boolean handleLevelUp() {
		RSInterfaceChild inter = Interfaces.get(233, 3);
		if (inter != null) {
			defaultDynamicSleep();
			if (!Timing.waitCondition(() -> {
				inter.click();
				General.sleep(200, 500);
				return Interfaces.get(233, 3) == null;
			}, 5000)) {
				General.println("AutoGeneral_Error - Could not discard the level up box.");
				return false;
			}

			shortDynamicSleep(); // Adding a little extra sleep ontop of our already short sleep if we managed to close the window.
			return true;
		}
		
		return false;
	}
	
	public void superDynamicSleeper(int min_often, int max_often, int min_seldom, int max_seldom, int min_occurence, int max_occurence, boolean shift)
	{
		if (min_often >= max_often || min_often>= min_seldom || max_often >= max_seldom ||
			min_seldom >= max_seldom || min_occurence >= max_occurence || min_occurence == 0)
		{
			General.println("AutoGeneral_Error - Invalid parameter values.");
			return;
		}
		
		int occurence = General.random(min_occurence, max_occurence);

		// shift randomizes occurence even further so that the occurence does not always vary between the same min/max occurence.
		if (shift) {		
			if (min_occurence > 5 && max_occurence - min_occurence > 6)
				occurence = General.random(General.random(min_occurence - General.random(0, 3), min_occurence + General.random(1, 3)),
								           General.random(max_occurence - General.random(0, 3), max_occurence + General.random(1, 3)));
			else if (min_occurence > 2 && max_occurence - min_occurence > 4)
				occurence = General.random(General.random(min_occurence - General.random(0, 2), min_occurence + General.random(1, 2)),
									       General.random(max_occurence - General.random(0, 2), max_occurence + General.random(1, 2)));
			else if (min_occurence > 2) {
				occurence = General.random(General.random(min_occurence - General.random(0, 2), min_occurence + General.random(1, 2)),
					       				   General.random(max_occurence - General.random(0, 2), max_occurence + General.random(1, 2)));
			}
			else if (min_occurence == 2)
				occurence = General.random(General.random(General.random(min_occurence - General.random(0, 1), min_occurence + 1),
												   		  General.random(max_occurence - General.random(0, 1), max_occurence + 1)),   
										   General.random(General.random(min_occurence - General.random(1, 1), max_occurence + General.random(1, 2)),
												   		  General.random(max_occurence - General.random(1, 1), max_occurence + General.random(1, 2))));									
		}
		
		int rand = General.random(0, occurence);
		
		if (rand != occurence)
			General.sleep(min_often, max_often);
		else
			General.sleep(min_seldom, max_seldom);
	}

	public void superDynamicSleeper(int min_often, int max_often, int min_seldom, int max_seldom, int min_occurence, int max_occurence)
	{
		superDynamicSleeper(min_often,  max_often, min_seldom, max_seldom, min_occurence, max_occurence, false);
	}
	
	public void defaultDynamicSleep()
	{
		if (General.randomBoolean())
			superDynamicSleeper(330, 570, 1320, 2280, 6, 13, true);
		else
			superDynamicSleeper(330, 570, 660, 1140, 3, 5, true);
	}
	
	public void shortDynamicSleep() {
		if (General.randomBoolean())
			superDynamicSleeper(130, 270, 520, 1080, 6, 13, true);
		else
			superDynamicSleeper(130, 270, 260, 540, 3, 5, true);
	}
}
