package org.logic.transactions.model;

import org.apache.log4j.Logger;
import org.logic.exceptions.EntryExistsException;
import org.logic.transactions.model.bots.BotAvgOptionManager;
import org.logic.transactions.model.stoploss.modes.StopLossMode;

import javax.xml.bind.JAXBException;
import java.io.IOException;

public abstract class OptionManagerImpl<T extends OptionImpl> {

    private static Logger logger = Logger.getLogger(BotAvgOptionManager.class);

    public abstract void updateOption(OptionImpl option) throws IOException, JAXBException, EntryExistsException;

    public abstract int removeOptionByMarketNameAndMode(String marketName, StopLossMode mode) throws IOException, JAXBException;

    protected abstract void loadOptions() throws IOException, ClassNotFoundException, JAXBException;

    protected abstract void clearOptionCollection();

    protected abstract void addOption(T option) throws IOException, EntryExistsException, JAXBException;

    public boolean reload() {
        try {
            loadOptions();
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage() + "\n" + e.getStackTrace().toString());
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage() + "\n" + e.getStackTrace().toString());
        } catch (JAXBException e) {
            logger.error(e.getMessage() + "\n" + e.getStackTrace().toString());
        }
        return false;
    }
}
