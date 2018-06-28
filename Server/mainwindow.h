#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

private slots:
    void on_pushButton_clicked();

    void on_BtnConnectionRequest_clicked();

    void on_BtnEstablishConnection_clicked();

    void on_BtnImageFragment_clicked();

    void on_BtnDiscover_clicked();

private:
    Ui::MainWindow *ui;
};

#endif // MAINWINDOW_H
