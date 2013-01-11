package com.googlecode.gflot.examples.client.examples.decimation;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gflot.client.DataPoint;
import com.googlecode.gflot.client.PlotModel;
import com.googlecode.gflot.client.PlotModelStrategy;
import com.googlecode.gflot.client.PlotWidget;
import com.googlecode.gflot.client.Series;
import com.googlecode.gflot.client.SeriesHandler;
import com.googlecode.gflot.client.SimplePlot;
import com.googlecode.gflot.client.options.AxisOptions;
import com.googlecode.gflot.client.options.GlobalSeriesOptions;
import com.googlecode.gflot.client.options.LineSeriesOptions;
import com.googlecode.gflot.client.options.PlotOptions;
import com.googlecode.gflot.client.options.PointsSeriesOptions;
import com.googlecode.gflot.examples.client.examples.DefaultActivity;
import com.googlecode.gflot.examples.client.resources.Resources;
import com.googlecode.gflot.examples.client.source.SourceAnnotations.GFlotExamplesData;
import com.googlecode.gflot.examples.client.source.SourceAnnotations.GFlotExamplesRaw;
import com.googlecode.gflot.examples.client.source.SourceAnnotations.GFlotExamplesSource;

/**
 * @author Nicolas Morel
 */
@GFlotExamplesRaw( DecimationPlace.UI_RAW_SOURCE_FILENAME )
public class DecimationExample
    extends DefaultActivity
{

    private static Binder binder = GWT.create( Binder.class );

    interface Binder
        extends UiBinder<Widget, DecimationExample>
    {
    }

    /**
     * The Async interface of the service
     */
    @GFlotExamplesSource
    interface FakeRpcServiceAsync
    {
        void getNewData( AsyncCallback<DataPoint[]> callback );
    }

    /**
     * Plot
     */
    @GFlotExamplesData
    @UiField( provided = true )
    SimplePlot plot;

    /**
     * Button Start & Stop
     */
    @GFlotExamplesData
    @UiField
    Button startStop;

    private double previous = 0;

    private int timeCounter = 0;

    /**
     * Timer
     */
    @GFlotExamplesData
    private Timer updater;

    public DecimationExample( Resources resources )
    {
        super( resources );
    }

    /**
     * Create plot
     */
    @GFlotExamplesSource
    public Widget createPlot()
    {
        PlotModel model = new PlotModel();
        PlotOptions plotOptions = PlotOptions.create();
        plotOptions.setGlobalSeriesOptions( GlobalSeriesOptions.create()
            .setLineSeriesOptions( LineSeriesOptions.create().setLineWidth( 1 ).setShow( true ).setFill( true ) )
            .setPointsOptions( PointsSeriesOptions.create().setRadius( 2 ).setShow( true ) ).setShadowSize( 0d ) );
        plotOptions.addXAxisOptions( AxisOptions.create().setShow( false ) );

        final SeriesHandler series =
            model.addSeries( Series.of( "Random Series", "#003366" ), PlotModelStrategy.downSamplingStrategy( 20 ) );

        // pull the "fake" RPC service for new data
        updater = new Timer() {
            @Override
            public void run()
            {
                update( series, plot );
            }
        };

        // create the plot
        plot = new SimplePlot( model, plotOptions );

        return binder.createAndBindUi( this );
    }

    /**
     * Start the timer when the activity starts
     */
    @GFlotExamplesSource
    @Override
    public void start( AcceptsOneWidget panel, EventBus eventBus )
    {
        super.start( panel, eventBus );
        start();
    }

    /**
     * Stop the timer when the activity stops
     */
    @GFlotExamplesSource
    @Override
    public void onStop()
    {
        stop();
        super.onStop();
    }

    /**
     * On click on the start/stop button
     */
    @GFlotExamplesSource
    @UiHandler( "startStop" )
    void onClickStartStop( ClickEvent e )
    {
        if ( "Stop".equals( startStop.getText() ) )
        {
            stop();
        }
        else
        {
            start();
        }
    }

    /**
     * Start the timer
     */
    @GFlotExamplesSource
    private void start()
    {
        startStop.setText( "Stop" );
        updater.scheduleRepeating( 1000 );
    }

    /**
     * Stop the timer
     */
    @GFlotExamplesSource
    private void stop()
    {
        startStop.setText( "Start" );
        updater.cancel();
    }

    /**
     * Fake a rpc call and update the data
     */
    @GFlotExamplesSource
    private void update( final SeriesHandler series, final PlotWidget plot )
    {
        FakeRpcServiceAsync service = getRpcService();
        service.getNewData( new AsyncCallback<DataPoint[]>() {
            public void onFailure( Throwable caught )
            {
                GWT.log( "Something went wrong", caught );
            }

            public void onSuccess( DataPoint[] result )
            {
                for ( DataPoint dataPoint : result )
                {
                    series.add( dataPoint );
                }
                plot.redraw();
            }
        } );
    }

    /**
     * @return a fake rpc service
     */
    @GFlotExamplesSource
    private FakeRpcServiceAsync getRpcService()
    {
        return new FakeRpcServiceAsync() {
            public void getNewData( final AsyncCallback<DataPoint[]> callback )
            {
                double up = Random.nextDouble();
                double down = Random.nextDouble();
                callback.onSuccess( new DataPoint[] { DataPoint.of( timeCounter++, previous - down ),
                    DataPoint.of( timeCounter++, previous + up ) } );
                previous = previous + up;
            }
        };
    }
}
