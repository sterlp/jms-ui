import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'json'
})
export class JsonPipe implements PipeTransform {

  transform(value: any, args?: any): any {
    // tslint:disable-next-line: curly
    if (value == null) return null;
    return JSON.stringify(value);
  }
}
