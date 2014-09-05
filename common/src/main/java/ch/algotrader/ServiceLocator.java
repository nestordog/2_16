// license-header java merge-point
//
// Attention: Generated code! Do not modify by hand!
// Generated by: SpringServiceLocator.vsl in andromda-spring-cartridge.
//
package ch.algotrader;

import ch.algotrader.service.CalendarService;
import ch.algotrader.service.ChartProvidingService;
import ch.algotrader.service.CombinationService;
import ch.algotrader.service.FutureService;
import ch.algotrader.service.HistoricalDataService;
import ch.algotrader.service.LazyLoaderService;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.ManagementService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.MeasurementService;
import ch.algotrader.service.OptionService;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.PortfolioChartService;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.service.PositionService;
import ch.algotrader.service.PropertyService;
import ch.algotrader.service.SecurityRetrieverService;
import ch.algotrader.service.StrategyService;
import ch.algotrader.service.SubscriptionService;

/**
 * Locates and provides all available application services.
 */
public class ServiceLocator extends AbstractServiceLocator
{
    private ServiceLocator()
    {
        // shouldn't be instantiated
    }

    /**
     * The shared instance of this ServiceLocator.
     */
    private static final ServiceLocator instance = new ServiceLocator();

    /**
     * Gets the shared instance of this Class
     *
     * @return the shared service locator instance.
     */
    public static final ServiceLocator instance()
    {
        return instance;
    }


    /**
     * Gets an instance of {@link FutureService}.
     * @return FutureService from getContext().getBean("futureService")
     */
    public final FutureService getFutureService()
    {
        return getContext().getBean("futureService",FutureService.class);
    }

    /**
     * Gets an instance of {@link ManagementService}.
     * @return ManagementService from getContext().getBean("managementService")
     */
    public final ManagementService getManagementService()
    {
        return getContext().getBean("managementService",ManagementService.class);
    }

    /**
     * Gets an instance of {@link LazyLoaderService}.
     * @return LazyLoaderService from getContext().getBean("lazyLoaderService")
     */
    public final LazyLoaderService getLazyLoaderService()
    {
        return getContext().getBean("lazyLoaderService",LazyLoaderService.class);
    }

    /**
     * Gets an instance of {@link PositionService}.
     * @return PositionService from getContext().getBean("positionService")
     */
    public final PositionService getPositionService()
    {
        return getContext().getBean("positionService",PositionService.class);
    }

    /**
     * Gets an instance of {@link HistoricalDataService}.
     * @return HistoricalDataService from getContext().getBean("historicalDataService")
     */
    public final HistoricalDataService getHistoricalDataService()
    {
        return getContext().getBean("historicalDataService",HistoricalDataService.class);
    }

    /**
     * Gets an instance of {@link SecurityRetrieverService}.
     * @return SecurityRetrieverService from getContext().getBean("securityRetrieverService")
     */
    public final SecurityRetrieverService getSecurityRetrieverService()
    {
        return getContext().getBean("securityRetrieverService",SecurityRetrieverService.class);
    }

    /**
     * Gets an instance of {@link OptionService}.
     * @return OptionService from getContext().getBean("optionService")
     */
    public final OptionService getOptionService()
    {
        return getContext().getBean("optionService",OptionService.class);
    }

    /**
     * Gets an instance of {@link LookupService}.
     * @return LookupService from getContext().getBean("lookupService")
     */
    public final LookupService getLookupService()
    {
        return getContext().getBean("lookupService",LookupService.class);
    }

    /**
     * Gets an instance of {@link StrategyService}.
     * @return StrategyService from getContext().getBean("strategyService")
     */
    public final StrategyService getStrategyService()
    {
        return getContext().getBean("strategyService",StrategyService.class);
    }

    /**
     * Gets an instance of {@link MarketDataService}.
     * @return MarketDataService from getContext().getBean("marketDataService")
     */
    public final MarketDataService getMarketDataService()
    {
        return getContext().getBean("marketDataService",MarketDataService.class);
    }

    /**
     * Gets an instance of {@link OrderService}.
     * @return OrderService from getContext().getBean("orderService")
     */
    public final OrderService getOrderService()
    {
        return getContext().getBean("orderService",OrderService.class);
    }

    /**
     * Gets an instance of {@link CombinationService}.
     * @return CombinationService from getContext().getBean("combinationService")
     */
    public final CombinationService getCombinationService()
    {
        return getContext().getBean("combinationService",CombinationService.class);
    }

    /**
     * Gets an instance of {@link SubscriptionService}.
     * @return SubscriptionService from getContext().getBean("subscriptionService")
     */
    public final SubscriptionService getSubscriptionService()
    {
        return getContext().getBean("subscriptionService",SubscriptionService.class);
    }

    /**
     * Gets an instance of {@link MeasurementService}.
     * @return MeasurementService from getContext().getBean("measurementService")
     */
    public final MeasurementService getMeasurementService()
    {
        return getContext().getBean("measurementService",MeasurementService.class);
    }

    /**
     * Gets an instance of {@link PropertyService}.
     * @return PropertyService from getContext().getBean("propertyService")
     */
    public final PropertyService getPropertyService()
    {
        return getContext().getBean("propertyService",PropertyService.class);
    }

    /**
     * Gets an instance of {@link PortfolioService}.
     * @return PortfolioService from getContext().getBean("portfolioService")
     */
    public final PortfolioService getPortfolioService()
    {
        return getContext().getBean("portfolioService",PortfolioService.class);
    }

    /**
     * Gets an instance of {@link ChartProvidingService}.
     * @return ChartProvidingService from getContext().getBean("chartProvidingService")
     */
    public final ChartProvidingService getChartProvidingService()
    {
        return getContext().getBean("chartProvidingService",ChartProvidingService.class);
    }

    /**
     * Gets an instance of {@link PortfolioChartService}.
     * @return PortfolioChartService from getContext().getBean("portfolioChartService")
     */
    public final PortfolioChartService getPortfolioChartService()
    {
        return getContext().getBean("portfolioChartService",PortfolioChartService.class);
    }

    /**
     * Gets an instance of {@link CalendarService}.
     * @return CalendarService from getContext().getBean("calendarService")
     */
    public final CalendarService getCalendarService()
    {
        return getContext().getBean("calendarService",CalendarService.class);
    }

}
