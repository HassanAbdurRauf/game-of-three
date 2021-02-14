var stompClient = null;
var opponent = null;
var primaryPlayer = false;
var gameStarted = false;
var gameValue = 0;
var gameMessage = null;
var matchId = null;

/*
for some reason the stomp client
calls the error callback twice during a failure. The second call overrides
the message I will like to display.
This variable is being used as a hack to capture the original message
*/
var headerErrorMessage = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    $('#start').prop('disabled', !connected);
    $('#username').prop('disabled', connected);

    if (connected) {
        $("#gameplay").show();
        $('#start').prop('disabled', true).hide();
    } else {
        $("#gameplay").hide();
        hideGameControls();
    }
    $("#gameBoard").html("");
}

function connect() {
    clearError();

    $('#username').removeClass('invalid');

    var username = $.trim($('#username').val());

    if (username.length === 0) {
        $('#username').addClass('invalid');
        return;
    }


    var socket = new SockJS('/game-of-three');
    stompClient = Stomp.over(socket);
    stompClient.reconnect_delay = 0;
    stompClient.debug = function (str) {};

    stompClient.connect({
        username: username
    }, function (frame) {
        setConnected(true);

        start();
        stompClient.subscribe('/user/mq/events', function (response) {
            gameMessage = JSON.parse(response.body);
            processGameMessage();
        });

        stompClient.subscribe('/user/mq/errors', function (response) {
            showError(response.body);
        });

    }, showError);
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    primaryPlayer = false;
}

function showMessage(message) {
    $("#gameBoard").prepend('<tr><td colspan="2">' + message + '</td></tr>');
}

function showWonMessage() {
    $('#gameBoard').prepend('<tr><td colspan="2" class="alert alert-success">You won the game :)</td></tr>');
}

function showLostMessage(message) {
    $('#gameBoard').prepend('<tr><td colspan="2" class="alert alert-danger">You lost the game :(</td></tr>');
}

function start() {
    $('#gameBoard').html('');
    stompClient.send('/app/got.start', {});
}

function getNumberToMakeValueDivisibleByThree(value) {
    var modulo = value % 3;
    switch (modulo) {
        case 0:
            return 0;
        case 1:
            return -1;
        default:
            return 1;
    }
}

function startGameSession() {
    if (primaryPlayer && gameMessage.gameStatus === 'START') {
        $('#randomNumberSection').show();
    }

    if (gameMessage.gameStatus === 'PLAY') {
        $('#additionSelector').show();
    }

}

function processGameMessage() {
    if (null === gameMessage) {
        return;
    }
    clearError();
    hideGameControls();

    switch (gameMessage.gameStatus) {
        case 'WAITING':
            showMessage(gameMessage.content);
            gameMessage = null;
            break;
        case 'START':
            $('#gameBoard').html('');
            $('#opponentLabel').html("[You're playing against: <span>" + gameMessage.opponent + "</span>]").show();
            opponent = gameMessage.opponent;
            matchId = gameMessage.matchId;
            primaryPlayer = gameMessage.primaryPlayer;
            startGameSession();
            break;
        case 'PLAY':
            makeMove();
            break;
        case 'GAMEOVER':
            gameOver();
            break;
        case 'DISCONNECT':
            opponentDisconnected();
            break;
    }
}

function generateRandomNumber() {
    return Math.floor(Math.random() * 100) + 1;
}

function sendRandomNumber(randomNumber) {
    var message = {
        matchId: matchId,
        randomNumber: randomNumber
    };
    stompClient.send('/app/got.move', {}, JSON.stringify(message));
    gameMessage = null;
}

function makeMove() {
    var value = gameMessage.value;
    gameValue = value;
    showMessage(opponent + ' sent value ' + value);
    $('#additionSelector').show();
}

function sendMove(value, move) {
    showMessage('You added ' + move + ' to ' + value + ' to make it divisible by 3');
    var updatedValue = value + move;
    showMessage(updatedValue + ' divided by 3 = ' + updatedValue / 3);
    var message = {
        value: updatedValue,
        matchId: matchId
    };

    stompClient.send('/app/got.move', {}, JSON.stringify(message));
    gameMessage = null;
}

function gameOver() {

    if (gameMessage.winner) {
        showWonMessage();
    } else {
        showLostMessage();
    }

    hideGameControls();
    gameMessage = null;
}

function opponentDisconnected() {
    showMessage(gameMessage.content);
    $('#opponentLabel').html('').hide();
    matchId = null;
    hideGameControls();
    gameMessage = null;
}

function delay(fn) {
    setTimeout(fn, 1000);
}

function addNumber() {
    var selectedNumber = $('#addSelect option:selected').val();
    console.log(selectedNumber);
    var result = parseInt(gameValue) + parseInt(selectedNumber);

    if (result % 3 != 0) {
        showError('Addition should return a number divisible by 3');
        return;
    }

    sendMove(gameValue, parseInt(selectedNumber));
    hideGameControls();
}

function retrieveRandomNumber() {
    var randomNumber = parseInt($('#randomNumber').val());
    sendRandomNumber(randomNumber);
    hideGameControls();
}

function isAutomatic() {
    return $('#mode option:selected').text() === 'AUTOMATIC';
}

function hideGameControls() {
    $('#randomNumberSection').hide();
    $('#additionSelector').hide();
}

function clearError() {
    headerErrorMessage = null;
    $('#errorMessage').hide();
}

function showError(error) {

    if (error.headers && error.headers.message) {
        headerErrorMessage = error.headers.message;
    }

    if (headerErrorMessage !== null) {
        $('#errorMessage span').html(headerErrorMessage);
    } else {
        $('#errorMessage span').html(error);
    }

    $('#errorMessage').show();
}

$(function () {
    setConnected(false);

    $("form").on('submit', function (e) {
        e.preventDefault();
    });

    $("#connect").click(connect);
    $("#disconnect").click(disconnect);
    $('#start').click(function () {
        start();
        $(this).prop('disabled', true);
    });

    $('#btnNumberAdder').click(addNumber);
    $('#btnRandomNumber').click(retrieveRandomNumber);
});