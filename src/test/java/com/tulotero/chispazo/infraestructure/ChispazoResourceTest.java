package com.tulotero.chispazo.infraestructure;

import com.tulotero.chispazo.application.ChispazoDrawFinder;
import com.tulotero.chispazo.application.ChispazoBetPrizeCalculator;
import com.tulotero.chispazo.domain.ChispazoBet;
import com.tulotero.chispazo.domain.ChispazoDraw;
import com.tulotero.chispazo.domain.ChispazoPrizeCheck;
import com.tulotero.chispazo.domain.PrizeInfo;
import com.tulotero.chispazo.domain.ScrutinyPrize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;


class ChispazoResourceTest {
    @InjectMocks
    ChispazoResource resource;
    @Mock
    ChispazoDrawFinder drawFinder;
    @Mock
    ChispazoBetPrizeCalculator prizeCalculator;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    public void getGetDrawInfo_shouldReturnOkIfDrawIsFound() throws Exception {
        Long drawId = new Random().nextLong();
        ChispazoDraw draw = givenDraw(drawId);
        when(drawFinder.find(drawId)).thenReturn(Optional.of(draw));

        Response response = resource.getDrawInfo(drawId);

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), is(draw));
    }

    @Test
    public void getGetDrawInfo_shouldNotFoundIfDrawIsNotFound() throws Exception {
        Long drawId = new Random().nextLong();
        when(drawFinder.find(drawId)).thenReturn(Optional.empty());

        Response response = resource.getDrawInfo(drawId);

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void calculatePrizeInfo_shouldReturnPrizeOk() throws Exception {
        ChispazoBet bet = givenBet();
        Long drawId = new Random().nextLong();
        ChispazoDraw draw = givenDraw(drawId);
        when(drawFinder.find(drawId)).thenReturn(Optional.of(draw));
        ChispazoPrizeCheck prizeCheck = new ChispazoPrizeCheck(bet, draw);
        PrizeInfo prizeInfo = new PrizeInfo(10D, asList(1, 2, 3));
        when(prizeCalculator.calculatePrize(prizeCheck)).thenReturn(prizeInfo);

        Response response = resource.calculatePrizeInfo(drawId, bet);

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity(), is(prizeInfo));
        verify(prizeCalculator).calculatePrize(prizeCheck);
    }

    @Test
    public void calculatePrizeInfo_shouldReturnNotFoundIfDrawNotExists() throws Exception {
        ChispazoBet bet = givenBet();
        Long drawId = new Random().nextLong();
        when(drawFinder.find(drawId)).thenReturn(Optional.empty());

        Response response = resource.calculatePrizeInfo(drawId, bet);

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    private ChispazoDraw givenDraw(Long drawId){
        return ChispazoDraw.finished(
                drawId,
                LocalDateTime.now().minusDays(1),
                asList(5, 10, 15, 20, 25),
                asList(
                        new ScrutinyPrize(5, 1000000D),
                        new ScrutinyPrize(4, 100000D),
                        new ScrutinyPrize(3, 1000D),
                        new ScrutinyPrize(2, 10D),
                        new ScrutinyPrize(1, 1D)
                )
        );
    }

    private ChispazoBet givenBet(){
        return new ChispazoBet(
                asList(1,2,3,4,5)
        );
    }
}