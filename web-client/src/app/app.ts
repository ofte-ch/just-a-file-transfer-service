import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FileTransfer } from './components/file-transfer/file-transfer';

@Component({
    selector: 'app-root',
    imports: [RouterOutlet, FileTransfer],
    templateUrl: './app.html',
    styleUrl: './app.css',
})
export class App {
    protected readonly title = signal('client');
}
