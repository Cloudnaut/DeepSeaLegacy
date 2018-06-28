#include "mainwindow.h"
#include "ui_mainwindow.h"
#include <QUdpSocket>
#include <QtEndian>
#include <QFile>
#include <QDataStream>
#include <QBuffer>
#include <QDesktopWidget>
#include <winsock2.h>
#define IP "192.168.178.59"

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::on_pushButton_clicked()
{
    QUdpSocket *Socket = new QUdpSocket();
    Socket->writeDatagram(ui->TxtString->text().toUtf8(), QHostAddress(IP), 1066);

}

void MainWindow::on_BtnDiscover_clicked()
{
    QByteArray Payload;
    Payload.append("DSCS");
    Payload.append((char)0);

    QUdpSocket *Socket = new QUdpSocket();
    Socket->writeDatagram(Payload, QHostAddress(IP), 1066);
    delete Socket;
}


void MainWindow::on_BtnConnectionRequest_clicked()
{
    QByteArray Payload;
    Payload.append("DSCS");
    Payload.append((char)2);

    QUdpSocket *Socket = new QUdpSocket();
    Socket->writeDatagram(Payload, QHostAddress(IP), 1066);
    delete Socket;
}

void MainWindow::on_BtnEstablishConnection_clicked()
{
    QByteArray Payload;
    Payload.append("DSCS");
    Payload.append((char)4);

    uchar BufferWidth[4];
    uchar BufferHeight[4];
    qToBigEndian<int>(960, BufferWidth);
    qToBigEndian<int>(540, BufferHeight);

    Payload.append((char *)BufferWidth, 4);
    Payload.append((char *)BufferHeight, 4);

    QUdpSocket *Socket = new QUdpSocket();
    Socket->writeDatagram(Payload, QHostAddress(IP), 1066);
    delete Socket;
}

void MainWindow::on_BtnImageFragment_clicked()
{
    QPixmap CurrentRow;
    QUdpSocket *Socket = new QUdpSocket();
    Socket->socketOption(QAbstractSocket::LowDelayOption);

    while(true)
    {
        QPixmap Pixmap = QPixmap::grabWindow(QApplication::desktop()->winId());

        int NewX = 960;
        int NewY = 540;

        Pixmap = Pixmap.scaled(NewX, NewY);

        for(int i = 0; i < 18; i++)
        {
            QPixmap CurrentRow = Pixmap.copy(0, i * 30, Pixmap.width(), 30);
            QByteArray RowBytes;
            QBuffer Buffer(&RowBytes);
            Buffer.open(QIODevice::WriteOnly);
            CurrentRow.save(&Buffer, "JPG");

            QByteArray Payload;
            Payload.append("DSCS");
            Payload.append((char)5);

            uchar PosX[4];
            uchar PosY[4];
            uchar Length[4];
            qToBigEndian<int>(0, PosX);
            qToBigEndian<int>(i * 30, PosY);
            qToBigEndian<int>((int)Buffer.size(), Length);

            Payload.append((char *)PosX, 4);
            Payload.append((char *)PosY, 4);
            Payload.append((char *)Length, 4);
            Payload.append(RowBytes, RowBytes.size());

            Socket->writeDatagram(Payload, QHostAddress(IP), 1066);
        }
    }

    delete Socket;
}


